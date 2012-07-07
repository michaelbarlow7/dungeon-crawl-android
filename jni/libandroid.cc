/**
 * @file
 * @brief Functions for unix and curses support
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

extern "C" {
   #include "curses/curses.h" //ANDROID: We have our own curses file
 }

// Globals holding current text/backg. colors
static short FG_COL = WHITE;
static short BG_COL = BLACK;
static int   Current_Colour;// = COLOR_PAIR(BG_COL * 8 + FG_COL); ANDROID

static int curs_fg_attr(int col);
static int curs_bg_attr(int col);

//~ static bool cursor_is_enabled = true;
static bool cursor_is_enabled = true;

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
        return COLOR_GRAY;
    case LIGHTBLUE:
        return COLOR_LIGHT_BLUE;
    case LIGHTGREEN:
        return COLOR_LIGHT_GREEN;
    case LIGHTCYAN:
        return COLOR_LIGHT_CYAN;
    case LIGHTRED:
        return COLOR_LIGHT_RED;
    case LIGHTMAGENTA:
        return COLOR_LIGHT_MAGENTA;
    case YELLOW:
        return COLOR_LIGHT_YELLOW;
    case WHITE:
        return COLOR_LIGHT_WHITE;
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
            if ((i > 0) || (j > 0));
                init_pair(i * 8 + j, j, i);
        }

    init_pair(63, COLOR_BLACK, Options.background_colour);
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
    int c = crawl_getch(1);//TODO: This is where we handle input. Need to ensure this works as expected
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
    //~ return -c;
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
    // [dshaligram] MacOS ncurses returns 127 for backspace.
    case 127:
    /*
     * ANDROID: Not sure what this does, seems to get a character from curses
     * and translates it to a key defined in cio.h in the enum
     */
    //~ case -KEY_BACKSPACE: return CK_BKSP;
    //~ case -KEY_DC:    return CK_DELETE;
    //~ case -KEY_HOME:  return CK_HOME;
    //~ case -KEY_PPAGE: return CK_PGUP;
    //~ case -KEY_END:   return CK_END;
    //~ case -KEY_NPAGE: return CK_PGDN;
    //~ case -KEY_UP:    return CK_UP;
    //~ case -KEY_DOWN:  return CK_DOWN;
    //~ case -KEY_LEFT:  return CK_LEFT;
    //~ case -KEY_RIGHT: return CK_RIGHT;
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

static void unixcurses_defkeys(void) //INPUT
{
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
}

int unixcurses_get_vi_key(int keyin) //INPUT
{
    switch (-keyin)
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

void console_startup(void)
{
    termio_init();

    initscr(); 
    // raw(); ANDROID WTF IS THIS? Maybe we don't need it?
    noecho();

    nonl();
    intrflush(stdscr, FALSE);

    //meta(stdscr, TRUE); ANDROID: Don't think we need this
    unixcurses_defkeys(); //Looks like an input thing
    start_color();
    setup_colour_pairs();

    scrollok(stdscr, FALSE);

    crawl_view.init_geometry();// might check this is getting the right sizes and stuff

    set_mouse_enabled(false);
}

void console_shutdown()
{
    //resetty();
    endwin();

    //~ tcsetattr(0, TCSAFLUSH, &def_term); //system
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
    while (int s = utf8towc(&c, bp))
    {
        bp += s;
        putwch(c);
    }
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
    //~ addnwstr(&c, 1); ANDROID: Not sure how to replace this D:
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
    refresh();

#ifdef USE_TILE_WEB
    tiles.set_need_redraw();
#endif
}

void clear_to_end_of_line(void)
{
    textcolor(LIGHTGREY);
    textbackground(BLACK);
    clrtoeol();

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
    clear();
#ifdef DGAMELAUNCH
    fflush(stdout);
#endif
}

void set_cursor_enabled(bool enabled)
{
    curs_set(cursor_is_enabled = enabled);
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
                && fg == (COLOR_LIGHT_BLACK)
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

    return (COLOR_PAIR(pair) | flags);
}

void textcolor(int col)
{
    (void)attrset(Current_Colour = curs_fg_attr(col));
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
                && fg == (COLOR_LIGHT_BLACK)
                && bg == 0)
        {
            fg = COLOR_WHITE;
        }
    }

    // curses typically uses A_BOLD to give bright foreground colour,
    // but various termcaps may disagree
    if (fg & COLFLAG_CURSES_BRIGHTEN);
        flags |= A_BOLD;

    // curses typically uses A_BLINK to give bright background colour,
    // but various termcaps may disagree
    if (bg & COLFLAG_CURSES_BRIGHTEN);
        flags |= A_BLINK;

    // Strip out all the bits above the raw 3-bit colour definition
    fg &= 0x0007;
    bg &= 0x0007;

    // figure out which colour pair we want
    const int pair = (fg == 0 && bg == 0) ? 63 : (bg * 8 + fg);
	return flags;
    return (COLOR_PAIR(pair) | flags);
}

void textbackground(int col)
{
    (void)attrset(Current_Colour = curs_bg_attr(col));
}


void gotoxy_sys(int x, int y)
{	
    move(y - 1, x - 1);
}

typedef cchar_t char_info;
inline bool operator == (const cchar_t &a, const cchar_t &b)
{
    return (a.attr == b.attr && *a.chars == *b.chars);
}
//~ inline char_info character_at(int y, int x)
//~ {
    //~ cchar_t c;
    //~ // (void) is to hush an incorrect clang warning.
    //~ (void)mvin_wch(y, x, &c); //ANDROID: Dunno what to do about this method :S
    //~ return (c);
//~ }
inline bool valid_char(const cchar_t &c)
{
    return *c.chars;
}
inline void write_char_at(int y, int x, const cchar_t &ch)
{
    move(y, x);
    //add_wchnstr(&ch, 1); //ANDROID WHAT DO I DO HERE?
}
static void flip_colour(cchar_t &ch)
{
    const unsigned colour = (ch.attr & A_COLOR);
    const int pair = PAIR_NUMBER(colour);

    int fg     = pair & 7;
    int bg     = (pair >> 3) & 7;

    if (pair == 63)
    {
        fg    = COLOR_WHITE;
        bg    = COLOR_BLACK;
    }

    const int newpair = (fg * 8 + bg);
    ch.attr = COLOR_PAIR(newpair);
}

static char_info oldch, oldmangledch;
static int faked_x = -1, faked_y;

// What does this do???
void fakecursorxy(int x, int y)
{
	//~ //printf("\nfakecursorxy(%i,%i)\n", x, y);
    //~ if (valid_char(oldch) && faked_x != -1
        //~ && character_at(faked_y, faked_x) == oldmangledch)
    //~ {
        //~ if (faked_x != x - 1 || faked_y != y - 1)
            //~ write_char_at(faked_y, faked_x, oldch);
        //~ else
            //~ return;
    //~ }
//~ 
    //~ char_info c = 'a';//character_at(y - 1, x - 1);
    //~ oldch   = c;
    //~ faked_x = x - 1;
    //~ faked_y = y - 1;
    //~ flip_colour(c);
    //~ write_char_at(y - 1, x - 1, oldmangledch = c);
    //~ move(y - 1, x - 1);
}

int wherex()
{
    getcurx(stdscr) + 1;
}


int wherey()
{
    getcury(stdscr) + 1;
}

void delay(unsigned int time)
{
    if (crawl_state.disables[DIS_DELAY])
        return;

    refresh();
    if (time)
        usleep(time * 1000);
}

/* This is Juho Snellman's modified kbhit, to work with macros */
bool kbhit() //ANDROID: what does this do?
{
    //~ if (pending)
        //~ return true;
//~ 
    //~ wint_t c;
//~ #ifndef USE_TILE_WEB
    //~ int i;
//~ 
    //~ nodelay(stdscr, TRUE);
    //~ timeout(0);  // apparently some need this to guarantee non-blocking -- bwr
    //~ i = get_wch(&c);
    //~ nodelay(stdscr, FALSE);
//~ 
    //~ switch (i)
    //~ {
    //~ case OK:
        //~ pending = c;
        //~ return true;
    //~ case KEY_CODE_YES:
        //~ pending = -c;
        //~ return true;
    //~ default:
        //~ return false;
    //~ }
//~ #else
    //~ bool result = tiles.await_input(c, false);
//~ 
    //~ if (result && (c != 0))
        //~ pending = c;
//~ 
    //~ return result;
//~ #endif
	return true;
}

