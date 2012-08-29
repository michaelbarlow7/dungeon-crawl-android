/**
 * @file
 * @brief Functions for android support
**/

/* Some replacement routines missing in gcc
   Some of these are inspired by/stolen from the Linux-conio package
   by Mental EXPlotion. Hope you guys don't mind.
   The colour exchange system is perhaps a little overkill, but I wanted
   it to be general to support future changes.. The only thing not
   supported properly is black on black (used by curses for "normal" mode)
   and white on white (used by me for "bright black" (darkgrey) on black

   Jan 1998 Svante Gerhard <svante@algonet.se>                          */
#include "AppHdr.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdarg.h>
#include <ctype.h>
#define _LIBUNIX_IMPLEMENTATION
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
//#include <langinfo.h> ANDROID
#include <termios.h>

static struct termios def_term;
static struct termios game_term;

#ifdef USE_UNIX_SIGNALS
#include <signal.h>
#endif

#include <time.h>
#include <jni.h>
#include <setjmp.h>

#define LINES 24
#define COLS 80

#define COLOR_BLACK 0
#define COLOR_BLUE 1
#define COLOR_GREEN 2
#define COLOR_CYAN 3
#define COLOR_RED 4
#define COLOR_MAGENTA 5
#define COLOR_YELLOW 6
#define COLOR_WHITE 7

#define COLOR_GRAY 8
#define COLOR_LIGHT_BLACK 8
#define COLOR_LIGHT_BLUE 9
#define COLOR_LIGHT_GREEN 10
#define COLOR_LIGHT_CYAN 11
#define COLOR_LIGHT_RED 12
#define COLOR_LIGHT_MAGENTA 13
#define COLOR_LIGHT_YELLOW 14
#define COLOR_LIGHT_WHITE 15

#define A_NORMAL 0
#define A_REVERSE 0x100
#define A_STANDOUT 0x200
#define A_BOLD 0x400
#define A_UNDERLINE 0x800
#define A_BLINK 0x1000
#define A_DIM 0x2000

// Adding stuff for ANDROID TODO: Probably a redundant conversion, since it gets converted later on, 
// but it's here for now to keep the curses interface consistent
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

//~#include <android/log.h> //ANDROID: Turn this off when we're not using it!!
//~ extern "C" {
   //~ #include "curses/curses.h" //ANDROID: We have our own curses file
 //~ }


// Globals holding current text/backg. colors
static short FG_COL = WHITE;
static short BG_COL = BLACK;
static int   Current_Colour = BG_COL * 8 + FG_COL;// ANDROID

static int curs_fg_attr(int col);
static int curs_bg_attr(int col);

//~ static bool cursor_is_enabled = true;
static bool cursor_is_enabled = true;

static jmp_buf jbuf;
/* JVM enviroment */
static JavaVM *jvm;
static JNIEnv *env;

static jclass NativeWrapperClass;
static jobject NativeWrapperObj;

/* Java Methods */
static jmethodID NativeWrapper_fatal;
static jmethodID NativeWrapper_warn;
static jmethodID NativeWrapper_waddnstr;
static jmethodID NativeWrapper_wattrset;
static jmethodID NativeWrapper_wattrget;
static jmethodID NativeWrapper_overwrite;
static jmethodID NativeWrapper_touchwin;
static jmethodID NativeWrapper_whline;
static jmethodID NativeWrapper_wclear;
static jmethodID NativeWrapper_wclrtoeol;
static jmethodID NativeWrapper_wclrtobot;
static jmethodID NativeWrapper_noise;
static jmethodID NativeWrapper_init_color;
static jmethodID NativeWrapper_init_pair;
static jmethodID NativeWrapper_initscr;
static jmethodID NativeWrapper_newwin;
static jmethodID NativeWrapper_delwin;
static jmethodID NativeWrapper_scroll;
static jmethodID NativeWrapper_wrefresh;
static jmethodID NativeWrapper_getch;
static jmethodID NativeWrapper_wmove;
static jmethodID NativeWrapper_mvwinch;
static jmethodID NativeWrapper_curs_set;
static jmethodID NativeWrapper_flushinp;
static jmethodID NativeWrapper_getcury;
static jmethodID NativeWrapper_getcurx;
static jmethodID NativeWrapper_fakecursorxy;
// #ifdef ANGDROID_NIGHTLY
static jmethodID NativeWrapper_wctomb;
static jmethodID NativeWrapper_mbstowcs;
static jmethodID NativeWrapper_wcstombs;
// #endif
static jmethodID NativeWrapper_score_start;
static jmethodID NativeWrapper_score_detail;
static jmethodID NativeWrapper_score_submit;

void init_curses( JNIEnv* env1, jobject obj1 )
{
	env = env1;

	/* Save objects */
	NativeWrapperObj = obj1;

	/* Get NativeWrapper class */
	NativeWrapperClass = env->GetObjectClass(NativeWrapperObj);

	/* NativeWrapper Methods */
	NativeWrapper_fatal = JAVA_METHOD("fatal", "(Ljava/lang/String;)V");	
	NativeWrapper_warn = JAVA_METHOD("warn", "(Ljava/lang/String;)V");
	NativeWrapper_waddnstr = JAVA_METHOD("waddnstr", "(II[B)V");
	NativeWrapper_wattrset = JAVA_METHOD("wattrset", "(II)V");
	NativeWrapper_wattrget = JAVA_METHOD("wattrget", "(III)I");
	NativeWrapper_overwrite = JAVA_METHOD("overwrite", "(II)V");
	NativeWrapper_touchwin = JAVA_METHOD("touchwin", "(I)V");
	NativeWrapper_whline = JAVA_METHOD("whline", "(IBI)V");
	NativeWrapper_wclrtobot = JAVA_METHOD("wclrtobot", "(I)V");
	NativeWrapper_wclrtoeol = JAVA_METHOD("wclrtoeol", "(I)V");
	NativeWrapper_wclear = JAVA_METHOD("wclear", "(I)V");
	NativeWrapper_noise = JAVA_METHOD("noise", "()V");
	NativeWrapper_initscr = JAVA_METHOD("initscr", "()V");
	NativeWrapper_wrefresh = JAVA_METHOD("wrefresh", "(I)V");
	NativeWrapper_getch = JAVA_METHOD("getch", "(I)I");
	NativeWrapper_getcury = JAVA_METHOD("getcury", "(I)I");
	NativeWrapper_getcurx = JAVA_METHOD("getcurx", "(I)I");
	NativeWrapper_init_color = JAVA_METHOD("init_color", "(II)V");
	NativeWrapper_init_pair = JAVA_METHOD("init_pair", "(III)V");
	NativeWrapper_newwin = JAVA_METHOD("newwin", "(IIII)I");
	NativeWrapper_delwin = JAVA_METHOD("delwin", "(I)V");
	NativeWrapper_scroll = JAVA_METHOD("scroll", "(I)V");
	NativeWrapper_wmove = JAVA_METHOD("wmove", "(III)V");
	NativeWrapper_mvwinch = JAVA_METHOD("mvwinch", "(III)I");
	NativeWrapper_curs_set = JAVA_METHOD("curs_set", "(I)V");
	NativeWrapper_flushinp = JAVA_METHOD("flushinp", "()V");
	NativeWrapper_fakecursorxy = JAVA_METHOD("fakecursorxy","(III)V");
// #ifdef ANGDROID_NIGHTLY
	NativeWrapper_wctomb = JAVA_METHOD("wctomb", "([BB)I");
	NativeWrapper_mbstowcs = JAVA_METHOD("mbstowcs", "([B[BI)I");
	NativeWrapper_wcstombs = JAVA_METHOD("wcstombs", "([B[BI)I");
// #endif
	NativeWrapper_score_start = JAVA_METHOD("score_start", "()V");
	NativeWrapper_score_detail = JAVA_METHOD("score_detail", "([B[B)V");
	NativeWrapper_score_submit = JAVA_METHOD("score_submit", "([B[B)V");

	// process argc/argv 
	//~ jstring argv0 = NULL;
	//~ int i;
	//~ for(i = 0; i < argc; i++) {
		//~ argv0 = (*env)->GetObjectArrayElement(env, argv, i);
		//~ const char *copy_argv0 = (*env)->GetStringUTFChars(env, argv0, 0);
//~ 
		//~ LOGD("argv%d = %s",i,copy_argv0);
		//~ //angdroid_process_argv(i,copy_argv0); ANGDROID STUFF
//~ 
		//~ (*env)->ReleaseStringUTFChars(env, argv0, copy_argv0);
	//~ }
//~ 
	//~ if (!setjmp(jbuf))
		//~ angdroid_main(); ANGDROID STUFF
	//~ else
		//~ ; //longjmp to here
	//~ LOGD("Curses initialized");
}
//ANDROID STUFF BEGINS HERE
extern "C" 
{
	void Java_com_crawlmb_NativeWrapper_initGame( JNIEnv* env, jobject object , jstring jInitLocation);
};

void Java_com_crawlmb_NativeWrapper_initGame( JNIEnv* env, jobject object , jstring jInitLocation)
{
	init_curses(env, object);
	const char *constInitLocation = env->GetStringUTFChars(jInitLocation, NULL);
	char *initLocation = new char[strlen(constInitLocation) + 1];
	strncpy (initLocation, constInitLocation, strlen(constInitLocation));
	initLocation[strlen(constInitLocation)] = '\0';
	int argc = 3;
	char *argv[] = {"","-rc", initLocation};
	main (argc, argv);
}

static unsigned int convert_to_curses_attr(int chattr)
{
    switch (chattr & CHATTR_ATTRMASK)
    {
    case CHATTR_STANDOUT:       return (A_STANDOUT);
    case CHATTR_BOLD:           return (A_BOLD);
    case CHATTR_BLINK:          return (A_BLINK);
    case CHATTR_UNDERLINE:      return (A_UNDERLINE);
    case CHATTR_REVERSE:        return (A_REVERSE);
    case CHATTR_DIM:            return (A_DIM);
    default:                    return (A_NORMAL);
    }
}

static inline short macro_colour(short col)
{
    return (Options.colour[ col ]);
}

// Translate DOS colors to curses.
static short translate_colour(short col)
{
	switch (col)
    {
    case BLACK:
        return COLOR_BLACK;
    case BLUE:
        return COLOR_BLUE;
    case GREEN:
        return COLOR_GREEN;
    case CYAN:
        return COLOR_CYAN;
    case RED:
        return COLOR_RED;
    case MAGENTA:
        return COLOR_MAGENTA;
    case BROWN:
        return COLOR_YELLOW;
    case LIGHTGREY:
        return COLOR_WHITE;
    case DARKGREY:
        return COLOR_BLACK + COLFLAG_CURSES_BRIGHTEN;
    case LIGHTBLUE:
        return COLOR_BLUE + COLFLAG_CURSES_BRIGHTEN;
    case LIGHTGREEN:
        return COLOR_GREEN + COLFLAG_CURSES_BRIGHTEN;
    case LIGHTCYAN:
        return COLOR_CYAN + COLFLAG_CURSES_BRIGHTEN;
    case LIGHTRED:
        return COLOR_RED + COLFLAG_CURSES_BRIGHTEN;
    case LIGHTMAGENTA:
        return COLOR_MAGENTA + COLFLAG_CURSES_BRIGHTEN;
    case YELLOW:
        return COLOR_YELLOW + COLFLAG_CURSES_BRIGHTEN;
    case WHITE:
        return COLOR_WHITE + COLFLAG_CURSES_BRIGHTEN;
    default:
        return COLOR_GREEN;
    }
}

static void setup_colour_pairs(void)
{
    short i, j;

    for (i = 0; i < 8; i++)
        for (j = 0; j < 8; j++)
        {
            if ((i > 0) || (j > 0))
            {
				//~ init_pair(i * 8 + j, j, i);
				JAVA_CALL(NativeWrapper_init_pair, i * 8 + j, j, i);
			}
        }

    //~ init_pair(63, COLOR_BLACK, Options.background_colour);
    JAVA_CALL(NativeWrapper_init_pair, 63, COLOR_BLACK, Options.background_colour);
}

static void unix_handle_terminal_resize();

static void termio_init()// ANDROID: Input/terminal method. 
{
    //~ tcgetattr(0, &def_term);
    //~ memcpy(&game_term, &def_term, sizeof(struct termios));

    //~ def_term.c_cc[VINTR] = (char) 3;        // ctrl-C
    //~ game_term.c_cc[VINTR] = (char) 3;       // ctrl-C
//~ 
    //~ // Let's recover some control sequences
    //~ game_term.c_cc[VSTART] = (char) -1;     // ctrl-Q
    //~ game_term.c_cc[VSTOP] = (char) -1;      // ctrl-S
    //~ game_term.c_cc[VSUSP] = (char) -1;      // ctrl-Y
//~ #ifdef VDSUSP
    //~ game_term.c_cc[VDSUSP] = (char) -1;     // ctrl-Y
//~ #endif
//~ 
    //~ tcsetattr(0, TCSAFLUSH, &game_term); //termios function
//~ 
    crawl_state.terminal_resize_handler = unix_handle_terminal_resize;
}

void set_mouse_enabled(bool enabled)
{
	return;
}

static int pending = 0;

int getchk()
{
    if (pending)
    {
        int c = pending;
        pending = 0;
        return c;
    }

    //~ wint_t c;

    //switch (get_wch(&c))
    //~ int c = crawl_getch(1);//TODO: This is where we handle input. Need to ensure this works as expected
    int c = JAVA_CALL_INT(NativeWrapper_getch, 1);
    //~ switch (c)
    //~ {
    //~ case ERR:
        //~ // getch() returns -1 on EOF, convert that into an Escape. Evil hack,
        //~ // but the alternative is to explicitly check for -1 everywhere where
        //~ // we might otherwise spin in a tight keyboard input loop.
        //~ return ESCAPE;
    //~ case OK:
        //~ // a normal (printable) key
        //~ return c;
    //~ }
//~ 
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

int getch_ck() //ANDROID: Input
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
//ANDROID: Not sure what unix signals are exactly. I guess it uses signals.h
#if defined(USE_UNIX_SIGNALS)

static void handle_sigwinch(int)
{
    crawl_state.last_winch = time(0);
    if (crawl_state.waiting_for_command)
        handle_terminal_resize();
    else
        crawl_state.terminal_resized = true;
}

#endif // USE_UNIX_SIGNALS

static void unix_handle_terminal_resize()
{
    console_shutdown();
    console_startup();
}

//~ static void unixcurses_defkeys(void) //INPUT
//~ {
	//ANDROID: define_key is a curses thing. The precompiler might filter this out, but whatevs
//~ #ifdef NCURSES_VERSION
    //~ // keypad 0-9 (only if the "application mode" was successfully initialised)
    //~ define_key("\033Op", 1000);
    //~ define_key("\033Oq", 1001);
    //~ define_key("\033Or", 1002);
    //~ define_key("\033Os", 1003);
    //~ define_key("\033Ot", 1004);
    //~ define_key("\033Ou", 1005);
    //~ define_key("\033Ov", 1006);
    //~ define_key("\033Ow", 1007);
    //~ define_key("\033Ox", 1008);
    //~ define_key("\033Oy", 1009);
//~ 
    //~ // non-arrow keypad keys (for macros)
    //~ define_key("\033OM", 1010); // Enter
    //~ define_key("\033OP", 1011); // NumLock
    //~ define_key("\033OQ", 1012); // /
    //~ define_key("\033OR", 1013); // *
    //~ define_key("\033OS", 1014); // -
    //~ define_key("\033Oj", 1015); // *
    //~ define_key("\033Ok", 1016); // +
    //~ define_key("\033Ol", 1017); // +
    //~ define_key("\033Om", 1018); // .
    //~ define_key("\033On", 1019); // .
    //~ define_key("\033Oo", 1020); // -
//~ 
    //~ // variants.  Ugly curses won't allow us to return the same code...
    //~ define_key("\033[1~", 1031); // Home
    //~ define_key("\033[4~", 1034); // End
    //~ define_key("\033[E",  1040); // center arrow
//~ #endif
//~ }

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

// Certain terminals support vt100 keypad application mode only after some
// extra goading.
#define KPADAPP "\033[?1051l\033[?1052l\033[?1060l\033[?1061h"
#define KPADCUR "\033[?1051l\033[?1052l\033[?1060l\033[?1061l"

int start_color() 
{
	int colors = 16;

	int color_table[] = {
		0xFF000000, //BLACK
		0xFF0040FF, //BLUE
		0xFF008040, //GREEN
		0xFF00A0A0, //CYAN
		0xFFFF4040, //RED
		0xFF9020FF, //MAGENTA
		0xFFA64800, //YELLOW 
		0xFFC0C0C0, //WHITE
		0xFF606060, //BRIGHT_BLACK (GRAY)
		0xFF00FFFF, //BRIGHT_BLUE
		0xFF00FF00, //BRIGHT_GREEN
		0xFF20FFDC, //BRIGHT_CYAN
		0xFFFF5050, //BRIGHT_RED
		0xFFFA4FFD, //BRIGHT_MAGENTA
		0xFFFFFF00, //BRIGHT_YELLOW
		0xFFFFFFFF  //BRIGHT_WHITE
	};

	int i;
	for(i=0; i<colors; i++)
	{
		//~ init_color(i, color_table[i]);
		JAVA_CALL(NativeWrapper_init_color, i, color_table[i]);
	}

	return 0;
}
void console_startup(void)
{
    termio_init();

    //~ initscr(); 
    // raw(); ANDROID WTF IS THIS? Maybe we don't need it?
    //~ noecho();

    //~ nonl();
    //~ intrflush(stdscr, FALSE);

    //meta(stdscr, TRUE); ANDROID: Don't think we need this
    //~ unixcurses_defkeys(); //Looks like an input thing
    start_color();
    setup_colour_pairs();

    //~ scrollok(stdscr, FALSE);

    crawl_view.init_geometry();// might check this is getting the right sizes and stuff

    set_mouse_enabled(false);
}

void console_shutdown()
{
    //resetty();
    //~ endwin();

    //~ tcsetattr(0, TCSAFLUSH, &def_term); //system
}


void crawl_quit(const char* msg) 
{
	if (msg) 
	{
		//~ LOGE(msg);
		JAVA_CALL(NativeWrapper_fatal, env->NewStringUTF(msg));
	}

	if (crawl_quit_hook)
	{
		(*crawl_quit_hook)();
	}

	longjmp(jbuf,1);
}

//~ int addnstr(int n, const char *s) 
//~ {
	//~ waddnstr(stdscr, n, s);
	//~ jbyteArray array = env->NewByteArray(n);
	//~ if (array == NULL) crawl_quit("Error: Out of memory");
	//~ env->SetByteArrayRegion(array, 0, n, s);
	//~ LOGC("curses.waddnstr %d %d %c",w->w,n,s[0]);
	//~ JAVA_CALL(NativeWrapper_waddnstr, 0, n, array);
	//~ env->DeleteLocalRef(array);
	//~ return 0;
//~ }

int addnstr(int n, const char *s) 
{
	jbyteArray array = env->NewByteArray(n);
	if (array == NULL) crawl_quit("Error: Out of memory");
	env->SetByteArrayRegion(array, 0, n, (const jbyte *) s);//TODO: Check this added cast works
	//~ LOGC("curses.waddnstr %d %d %c",w->w,n,s[0]);
	//~ JAVA_CALL(NativeWrapper_waddnstr, 0, n, array);
	env->CallVoidMethod(NativeWrapperObj, NativeWrapper_waddnstr, 0, n, array);
	env->DeleteLocalRef(array);
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
        //~ putwch(c);
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
        
    // TODO: recognize unsupported characters and try to transliterate
    //~ addnwstr(&c, 1); 
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

#ifdef USE_TILE_WEB
    tiles.set_need_redraw();
#endif
}

void clear_to_end_of_line(void)
{
    textcolor(LIGHTGREY);
    textbackground(BLACK);
    //~ clrtoeol();
    JAVA_CALL(NativeWrapper_wclrtoeol, 0);

#ifdef USE_TILE_WEB
    tiles.clear_to_end_of_line();
#endif
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
    //~ clear();
    JAVA_CALL(NativeWrapper_wclear, 0);
#ifdef DGAMELAUNCH
    fflush(stdout);
#endif
}

void set_cursor_enabled(bool enabled)
{
    //~ curs_set(cursor_is_enabled = enabled);
    JAVA_CALL(NativeWrapper_curs_set, cursor_is_enabled = enabled);
}

bool is_cursor_enabled()
{
    return (cursor_is_enabled);
}

bool is_smart_cursor_enabled()
{
    return false;
}

void enable_smart_cursor(bool dummy)
{
}

inline unsigned get_brand(int col)//ANDROID: looks pretty harmless
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

static int curs_fg_attr(int col)
{
    short fg, bg;

    FG_COL = col & 0x00ff;
    fg = translate_colour(macro_colour(FG_COL));
    bg = translate_colour(BG_COL == BLACK ? Options.background_colour
                                           : BG_COL);

    // calculate which curses flags we need...
    unsigned int flags = 0;

    unsigned brand = get_brand(col);
    if (brand != CHATTR_NORMAL)
    {
        flags |= convert_to_curses_attr(brand);

        if ((brand & CHATTR_ATTRMASK) == CHATTR_HILITE)
        {
            bg = translate_colour(
                    macro_colour((brand & CHATTR_COLMASK) >> 8));

            if (fg == bg)
                fg = COLOR_BLACK;
        }

        // If we can't do a dark grey friend brand, then we'll
        // switch the colour to light grey.
        if (Options.no_dark_brand
                && fg == (COLOR_BLACK | COLFLAG_CURSES_BRIGHTEN)
                && bg == 0)
        {
            fg = COLOR_WHITE;
        }
    }

    // curses typically uses A_BOLD to give bright foreground colour,
    // but various termcaps may disagree
    if (fg & COLFLAG_CURSES_BRIGHTEN)
        flags |= A_BOLD;

    // curses typically uses A_BLINK to give bright background colour,
    // but various termcaps may disagree (in whole or in part)
    if (bg & COLFLAG_CURSES_BRIGHTEN)
        flags |= A_BLINK;

    // Strip out all the bits above the raw 3-bit colour definition
    fg &= 0x0007;
    bg &= 0x0007;

    // figure out which colour pair we want
    const int pair = (fg == 0 && bg == 0) ? 63 : (bg * 8 + fg);

    return (pair | flags);
}

void textcolor(int col)
{
    //~ (void)attrset(Current_Colour = curs_fg_attr(col));
    JAVA_CALL(NativeWrapper_wattrset,0, Current_Colour = curs_fg_attr(col));
}

static int curs_bg_attr(int col)
{
    short fg, bg;

    BG_COL = col & 0x00ff;
    fg = translate_colour(macro_colour(FG_COL));
    bg = translate_colour(BG_COL == BLACK ? Options.background_colour
                                           : BG_COL);

    unsigned int flags = 0;

    unsigned brand = get_brand(col);
    if (brand != CHATTR_NORMAL)
    {
        flags |= convert_to_curses_attr(brand);

        if ((brand & CHATTR_ATTRMASK) == CHATTR_HILITE)
        {
            bg = (brand & CHATTR_COLMASK) >> 8;
            if (fg == bg)
                fg = COLOR_BLACK;
        }

        // If we can't do a dark grey friend brand, then we'll
        // switch the colour to light grey.
        if (Options.no_dark_brand
                && fg == (COLOR_BLACK | COLFLAG_CURSES_BRIGHTEN)
                && bg == 0)
        {
            fg = COLOR_WHITE;
        }
    }

    // curses typically uses A_BOLD to give bright foreground colour,
    // but various termcaps may disagree
    if (fg & COLFLAG_CURSES_BRIGHTEN)
        flags |= A_BOLD;

    // curses typically uses A_BLINK to give bright background colour,
    // but various termcaps may disagree
    if (bg & COLFLAG_CURSES_BRIGHTEN)
        flags |= A_BLINK;

    // Strip out all the bits above the raw 3-bit colour definition
    fg &= 0x0007;
    bg &= 0x0007;

    // figure out which colour pair we want
    const int pair = (fg == 0 && bg == 0) ? 63 : (bg * 8 + fg);

    return (pair | flags);
}

void textbackground(int col)
{
    //~ (void)attrset(Current_Colour = curs_bg_attr(col));
    JAVA_CALL(NativeWrapper_wattrset,0, Current_Colour = curs_bg_attr(col));
}


void gotoxy_sys(int x, int y)
{	
    //~ move(y - 1, x - 1);
	JAVA_CALL(NativeWrapper_wmove, 0, y - 1, x - 1);
}

#define CCHARW_MAX	5

typedef unsigned long chtype;
typedef	chtype	attr_t;		/* ...must be at least as wide as chtype */
typedef struct
{
    attr_t	attr;
    wchar_t	chars[CCHARW_MAX];
}
cchar_t;
typedef cchar_t char_info;
inline bool operator == (const cchar_t &a, const cchar_t &b)
{
    return (a.attr == b.attr && *a.chars == *b.chars);
}
inline int character_at(int y, int x)
{
    int c;
    // (void) is to hush an incorrect clang warning.
    //~ (void)mvin_wch(y, x, &c); //ANDROID: Dunno what to do about this method :S
    //~ c = mvinch(y, x);
    c = JAVA_CALL_INT(NativeWrapper_mvwinch, 0, y, x);
    return (c);
}
inline bool valid_char(int c)
{
    return c != 0;
}
inline void write_char_at(int y, int x, int ch)
{
	//~ mvaddch(y, x, ch);
	char c = ch;
	JAVA_CALL(NativeWrapper_wmove, 0, y, x);
	addnstr(1,&c);
}

void fakecursorxy(int x, int y)
{
	//~ curses_fakecursorxy(x, y);
	JAVA_CALL(NativeWrapper_fakecursorxy, x, y, 0);
}

int wherex()
{
    //~ return getcurx(stdscr) + 1;
    return JAVA_CALL_INT(NativeWrapper_getcurx, 0) + 1;
}


int wherey()
{
    //~ return getcury(stdscr) + 1;
    return JAVA_CALL_INT(NativeWrapper_getcury, 0) + 1;
}

void delay(unsigned int time)
{
    if (crawl_state.disables[DIS_DELAY])
        return;

    //~ refresh();
    JAVA_CALL(NativeWrapper_wrefresh, 0);
    if (time)
        usleep(time * 1000);
}

/* This is Juho Snellman's modified kbhit, to work with macros */
bool kbhit()
{
	// I don't think we need this in android, buffering is handled by java code
	return false;
}

