/**
 * @file
 * @brief Functions for android support
**/

/* 
 * This is essentially the input/output adapter for the android code,
 * interfacing via the Java Native Interface.
 * Originally based off of libunix.cc

   Aug 2012 Michael Barlow <michaelbarlow7@gmail.com>                 */
#include "AppHdr.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdarg.h>
#include <ctype.h>
#include "libunix.h"
#include "defines.h"

#include "cio.h"
#include "delay.h"
#include "enum.h"
#include "externs.h"
#include "libutil.h"
#include "main.h"
#include "options.h"
#include "files.h"
#include "state.h"
#include "unicode.h"
#include "view.h"
#include "viewgeom.h"

#include <wchar.h>
#include <locale.h>
#include <termios.h>

#include <time.h>
#include <jni.h>
#include <setjmp.h>


#define LINES 24
#define COLS 80

// Probably a redundant conversion, since it gets converted later on, 
// but it's a bit of leftover code from the curses stuff
#define KEY_HOME	0406		/* home key */
#define KEY_END		0550		/* end key */
#define KEY_DOWN	0402		/* down-arrow key */
#define KEY_UP		0403		/* up-arrow key */
#define KEY_LEFT	0404		/* left-arrow key */
#define KEY_RIGHT	0405		/* right-arrow key */
#define KEY_NPAGE	0522		/* next-page key */
#define KEY_PPAGE	0523		/* previous-page key */
#define KEY_A1		0534		/* upper left of keypad */
#define KEY_A3		0535		/* upper right of keypad */
#define KEY_B2		0536		/* center of keypad */
#define KEY_C1		0537		/* lower left of keypad */
#define KEY_C3		0540		/* lower right of keypad */
#define KEY_SHOME	0607		/* shifted home key */
#define KEY_SEND	0602		/* shifted end key */
#define KEY_SLEFT	0611		/* shifted left-arrow key */
#define KEY_SRIGHT	0622		/* shifted right-arrow key */
#define KEY_BTAB	0541		/* back-tab key */
#define KEY_BACKSPACE	0407   /* backspace key */
#define KEY_DC		0512		/* delete-character key */

#define JAVA_CALL(...) (env->CallVoidMethod(NativeWrapperObj, __VA_ARGS__))
#define JAVA_CALL_INT(...) (env->CallIntMethod(NativeWrapperObj, __VA_ARGS__))
#define JAVA_METHOD(m,s) (env->GetMethodID(NativeWrapperClass, m, s))

void (*crawl_quit_hook)(void) = NULL;

static jmp_buf jbuf;
/* JVM enviroment */
static JavaVM *jvm;
static JNIEnv *env;

static jclass NativeWrapperClass;
static jobject NativeWrapperObj;

/* Java Methods */
static jmethodID NativeWrapper_fatal;
static jmethodID NativeWrapper_getch;
static jmethodID NativeWrapper_printTerminalChar;
static jmethodID NativeWrapper_invalidateTerminal;

// Terminal stuff
class TerminalChar //I guess this could be a struct. 
{
public:
	jint x; // If this were java
	jint y; // these two fields would be final
	jint foregroundColour;
	jint backgroundColour;
	jchar character;
	TerminalChar(){};
	TerminalChar(int py, int px)
	{
		x = px;
		y = py;
	}
};

std::map<COLORS, int> colorMap;
TerminalChar terminalWindow[LINES][COLS];
std::set<TerminalChar *> dirtyTerminalChars;
int x = 0;
int y = 0;
jint backgroundColour; //RGB values
jint foregroundColour;
unsigned brand;

void advance()
{
		++x;
		if (x >= COLS)
		{
			++y;
			x = 0;
		}
		if (y >= LINES)
		{
			y = LINES - 1;
		}
}

TerminalChar * getCurrentTerminalChar()
{
	return &terminalWindow[y][x];
}

void init_java_methods( JNIEnv* env1, jobject obj1 )
{
	env = env1;

	/* Save objects */
	NativeWrapperObj = obj1;

	/* Get NativeWrapper class */
	NativeWrapperClass = env->GetObjectClass(NativeWrapperObj);

	/* NativeWrapper Methods */
	NativeWrapper_fatal = JAVA_METHOD("fatal", "(Ljava/lang/String;)V");	
	NativeWrapper_getch = JAVA_METHOD("getch", "(I)I");
	NativeWrapper_printTerminalChar = JAVA_METHOD("printTerminalChar", "(IICII)V");
	NativeWrapper_invalidateTerminal = JAVA_METHOD("invalidateTerminal", "()V");
}

extern "C" 
{
	void Java_com_crawlmb_NativeWrapper_initGame( JNIEnv* env, jobject object , jstring jInitLocation);
	void Java_com_crawlmb_NativeWrapper_refreshTerminal( JNIEnv* env, jobject object);
};

void Java_com_crawlmb_NativeWrapper_initGame( JNIEnv* env, jobject object , jstring jInitLocation)
{
	init_java_methods(env, object);
	const char *constInitLocation = env->GetStringUTFChars(jInitLocation, NULL);
	char *initLocation = new char[strlen(constInitLocation) + 1];
	strncpy (initLocation, constInitLocation, strlen(constInitLocation));
	initLocation[strlen(constInitLocation)] = '\0';
	int argc = 3;
	char *argv[] = {"","-rc", initLocation};
	main (argc, argv);
}

void Java_com_crawlmb_NativeWrapper_refreshTerminal( JNIEnv* env, jobject object)
{
	for (int i = 0; i < LINES; ++i)
	{
		for (int j = 0; j < COLS; ++j)
		{
			TerminalChar * terminalChar = &terminalWindow[i][j];
			JAVA_CALL(NativeWrapper_printTerminalChar, terminalChar->y, terminalChar->x, terminalChar->character, terminalChar->foregroundColour, terminalChar->backgroundColour);
		}
	}
	JAVA_CALL(NativeWrapper_invalidateTerminal);
}

void set_mouse_enabled(bool enabled)
{
	return;
}

void sendTerminalToScreen()
{
	if (dirtyTerminalChars.empty())
	{
		return;
	}
	std::set<TerminalChar *>::iterator it;
	for (it = dirtyTerminalChars.begin(); it != dirtyTerminalChars.end(); it++)
	{
		JAVA_CALL(NativeWrapper_printTerminalChar, (*it)->y, (*it)->x, (*it)->character, (*it)->foregroundColour, (*it)->backgroundColour);
	}
	JAVA_CALL(NativeWrapper_invalidateTerminal);
	dirtyTerminalChars.clear();
}

static inline short macro_colour(short col)
{
    return (Options.colour[ col ]);
}

int getchk()
{
	sendTerminalToScreen();
    int c = JAVA_CALL_INT(NativeWrapper_getch, 1);
    return c;
}

int m_getch()
{
    int c;
    do
    {
        c = getchk();

    } while ((c == CK_MOUSE_MOVE || c == CK_MOUSE_CLICK)
             && !crawl_state.mouse_enabled);

    return (c);
}

int getch_ck() 
{
    int c = m_getch();
    switch (c)
    {
    // Android returns 159 for backspace and 156 for enter
    case 159:
    case KEY_BACKSPACE: return CK_BKSP;
    case 156: return CK_ENTER;
    case KEY_DC:    return CK_DELETE;
    case KEY_HOME:  return CK_HOME;
    case KEY_PPAGE: return CK_PGUP;
    case KEY_END:   return CK_END;
    case KEY_NPAGE: return CK_PGDN;
    case KEY_UP:    return CK_UP;
    case KEY_DOWN:  return CK_DOWN;
    case KEY_LEFT:  return CK_LEFT;
    case KEY_RIGHT: return CK_RIGHT;
    
    default:         return c;
    }
}

static void unix_handle_terminal_resize()
{
    console_shutdown();
    console_startup();
}

int unixcurses_get_vi_key(int keyin) //INPUT
{
    switch (keyin)
    {
    // -1001..-1009: passed without change
    case 1031: return -1007;
    case 1034: return -1001;
    case 1040: return -1005;
    case KEY_HOME:   return -1007;
    case KEY_END:    return -1001;
    case KEY_DOWN:   return -1002;
    case KEY_UP:     return -1008;
    case KEY_LEFT:   return -1004;
    case KEY_RIGHT:  return -1006;
    case KEY_NPAGE:  return -1003;
    case KEY_PPAGE:  return -1009;
    case KEY_A1:     return -1007;
    case KEY_A3:     return -1009;
    case KEY_B2:     return -1005;
    case KEY_C1:     return -1001;
    case KEY_C3:     return -1003;
    case KEY_SHOME:  return 'Y';
    case KEY_SEND:   return 'B';
    case KEY_SLEFT:  return 'H';
    case KEY_SRIGHT: return 'L';
    case KEY_BTAB:   return CK_SHIFT_TAB;
    }
    return keyin;
}

int start_color() 
{
	//~ colorMap.insert(std::pair<COLORS, int>(BLACK, 0xFF000000));
	colorMap[BLACK] = 0xFF000000; // This really should be global, or loaded in the onload, or whatever
	colorMap[BLUE] = 0xFF0040FF;
	colorMap[GREEN] = 0xFF008040;
	colorMap[CYAN] = 0xFF00A0A0;
	colorMap[RED] = 0xFFFF4040;
	colorMap[MAGENTA] = 0xFF9020FF;
	colorMap[BROWN] = 0xFFA64800;
	colorMap[LIGHTGRAY] = 0xFFC0C0C0;
	colorMap[DARKGRAY] = 0xFF606060;
	colorMap[LIGHTBLUE] = 0xFF00FFFF;
	colorMap[LIGHTGREEN] = 0xFF00FF00;
	colorMap[LIGHTCYAN] = 0xFF20FFDC;
	colorMap[LIGHTRED] = 0xFFFF5050;
	colorMap[LIGHTMAGENTA] = 0xFFFA4FFD;
	colorMap[YELLOW] = 0xFFFFFF00;
	colorMap[WHITE] = 0xFFFFFFFF;
	colorMap[MAX_TERM_COLOUR] = 0xFF008040;

	foregroundColour = colorMap[WHITE];
	backgroundColour = colorMap[BLACK];

	return 0;
}
void setUpTerminalCharacters()
{
	// Set up terminal window here
	for (int i = 0; i < LINES; ++i)
	{
		for (int j = 0; j < COLS; ++j)
		{//TODO: We'd ideally initialize all this in a constructor
			terminalWindow[i][j].x = j;
			terminalWindow[i][j].y = i;
			terminalWindow[i][j].character = ' ';
			terminalWindow[i][j].foregroundColour = colorMap[WHITE];
			terminalWindow[i][j].backgroundColour = colorMap[BLACK];
		}
	}
}
void console_startup(void)
{
    start_color();
    
    setUpTerminalCharacters();

    crawl_view.init_geometry();
}

void console_shutdown()
{
    // I don't think we need to do anything here for android
}


void crawl_quit(const char* msg) 
{
	if (msg) 
	{
		JAVA_CALL(NativeWrapper_fatal, env->NewStringUTF(msg));
	}

	if (crawl_quit_hook)
	{
		(*crawl_quit_hook)();
	}

	longjmp(jbuf,1);
}

void advanceLine()
{
	y++;
	if (y >= LINES)
	{
		y = LINES - 1;
	}
	x = 0;
}

void clear_to_end_of_line();
void addChar(char c)
{
	if (c == '\n')
	{
		// On a newline character, clear to the end of the line and 
		// advance a row
		clear_to_end_of_line();
		return;
	}
	
	// Need to determine colours depending on brand
	int fg = foregroundColour;
	int bg = backgroundColour;
	if (brand != CHATTR_NORMAL)
	{
		if ((brand & CHATTR_ATTRMASK) == CHATTR_HILITE)
		{
			COLORS bgcolor = (COLORS) macro_colour((brand & CHATTR_COLMASK) >> 8);
			bg = colorMap[bgcolor];
		}

		if ((brand & CHATTR_ATTRMASK) == CHATTR_REVERSE)
		{
			int temp = fg;
			fg = bg;
			bg = temp;
		}
		
		if (fg == bg)
		{
			fg = colorMap[BLACK];
		}
	}
	
	// Apply changes to terminalChar, if they apply
	bool isDirty = false;
	TerminalChar * terminalChar = getCurrentTerminalChar();
	if (terminalChar->foregroundColour != fg)
	{
		terminalChar->foregroundColour = fg;
		isDirty = true;
	}
	if (terminalChar->backgroundColour != bg)
	{
		terminalChar->backgroundColour = bg;
		isDirty = true;
	}
	if (terminalChar->character != c)
	{
		terminalChar->character = c;
		isDirty = true;
	}
	
	if (isDirty)
	{
		dirtyTerminalChars.insert(terminalChar);
	}
	advance();
}

int addnstr(int n, const char *s) 
{
	while (*s)
	{
		addChar(*s);
		++s;
	}
	return 0;
}

void cprintf(const char *format, ...)
{
    char buffer[2048];          // One full screen if no control seq...

    va_list argp;

    va_start(argp, format);
    vsnprintf(buffer, sizeof(buffer), format, argp);
    va_end(argp);
    
    ucs_t c;
    char *bp = buffer;
    int i = 0;
    while (int s = utf8towc(&c, bp))
    {
		i++;
        bp += s;
    }
    addnstr(i, buffer);
}


void putwch(ucs_t chr)
{
    wchar_t c = chr;
    if (!c)
    {
		c = ' ';
	}
	char * printstr = new char[1];
	sprintf(printstr, "%c", chr);
        
    addnstr(1, printstr);
}

void puttext(int x1, int y1, const crawl_view_buffer &vbuf)
{
    const screen_cell_t *cell = vbuf;
    const coord_def size = vbuf.size();
    for (int y = 0; y < size.y; ++y)
    {
        cgotoxy(x1, y1 + y);
        for (int x = 0; x < size.x; ++x)
        {
            put_colour_ch(cell->colour, cell->glyph);
            cell++;
        }
    }
    update_screen();
}

// These next four are front functions so that we can reduce
// the amount of curses special code that occurs outside this
// this file.  This is good, since there are some issues with
// name space collisions between curses macros and the standard
// C++ string class.  -- bwr
void update_screen(void)
{
    //ANDROID: We don't really need this I don't think
}

void clear_to_end_of_line(void)
{
    textcolor(LIGHTGREY);
    textbackground(BLACK);
    do
    {
		addChar(' ');
	} while (x > 0);
}

int get_number_of_lines(void)
{
    return (LINES);
}

int get_number_of_cols(void)
{
    return (COLS);
}

void clrscr()
{
    textcolor(LIGHTGREY);
    textbackground(BLACK);
    x = 0;
    y = 0;
    for (int i = 0; i < LINES; ++i)
    {
		for (int j = 0; j < COLS; ++j)
		{
			addChar(' ');
		}
	}
	x = 0;
	y = 0;
}

void set_cursor_enabled(bool enabled)
{
}

bool is_cursor_enabled()
{
    return true;
}

bool is_smart_cursor_enabled()
{
    return true;
}

void enable_smart_cursor(bool dummy)
{
}

inline unsigned get_brand(int col)
{
    return (col & COLFLAG_FRIENDLY_MONSTER) ? Options.friend_brand :
           (col & COLFLAG_NEUTRAL_MONSTER)  ? Options.neutral_brand :
           (col & COLFLAG_ITEM_HEAP)        ? Options.heap_brand :
           (col & COLFLAG_WILLSTAB)         ? Options.stab_brand :
           (col & COLFLAG_MAYSTAB)          ? Options.may_stab_brand :
           (col & COLFLAG_FEATURE_ITEM)     ? Options.feature_item_brand :
           (col & COLFLAG_TRAP_ITEM)        ? Options.trap_item_brand :
           (col & COLFLAG_REVERSE)          ? CHATTR_REVERSE
                                            : CHATTR_NORMAL;
}

void textcolor(int col)
{
	COLORS fgcolor = (COLORS) macro_colour(col & 0x00ff);
	brand = get_brand(col);
	foregroundColour = colorMap[fgcolor];
}

void textbackground(int col)
{
	COLORS bgcolor = (COLORS) macro_colour(col & 0x00ff);
	brand = get_brand(col);
	backgroundColour = colorMap[bgcolor];
}


void gotoxy_sys(int px, int py)
{
	x = px - 1;
	y = py - 1;
}

inline int character_at(int py, int px)
{
	gotoxy_sys(px, py);
	return getCurrentTerminalChar()->character;
}

inline void write_char_at(int py, int px, int ch)
{
	gotoxy_sys(px, py);
	
	char c = ch;
	addnstr(1,&c);
}

void fakecursorxy(int px, int py)
{
	gotoxy_sys(px, py);
	TerminalChar * flippingChar = getCurrentTerminalChar();
	int tempColor = flippingChar->foregroundColour;
	flippingChar->foregroundColour = flippingChar->backgroundColour;
	flippingChar->backgroundColour = tempColor;
	dirtyTerminalChars.insert(flippingChar);
}

int wherex()
{
	return x + 1;
}


int wherey()
{
	return y + 1;
}

void delay(unsigned int time)
{
    if (crawl_state.disables[DIS_DELAY])
        return;

	sendTerminalToScreen();
    if (time)
        usleep(time * 1000);
}

bool kbhit()
{
	// I don't think we need this in android, buffering is handled by java code
	return false;
}

