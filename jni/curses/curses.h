#include <jni.h>
#include <android/log.h>
#include <pthread.h>
#include <setjmp.h>

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
//#define A_ALTCHARSET 0x4000

#ifndef TRUE
#define TRUE -1
#define FALSE 0
#endif

#define COLOR_PAIR(x) (x)

#define LINES 24
#define COLS 80

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "Crawl", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "Crawl", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "Crawl", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "Crawl", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "Crawl", __VA_ARGS__) 
#define LOG(...) __android_log_print(ANDROID_LOG_DEBUG  , "Crawl", __VA_ARGS__)
// Adding stuff for ANDROID
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

#define NCURSES_ATTR_SHIFT       8
#define NCURSES_BITS(mask,shift) ((mask) << ((shift) + NCURSES_ATTR_SHIFT))
#define A_COLOR		NCURSES_BITS(((1UL) << 8) - 1UL,0)

#define NCURSES_CAST(type,value) static_cast<type>(value)
#define PAIR_NUMBER(a)	(NCURSES_CAST(int,((NCURSES_CAST(unsigned long,a) & A_COLOR) >> NCURSES_ATTR_SHIFT)))

/*
 * cchar_t stores an array of CCHARW_MAX wide characters.  The first is
 * normally a spacing character.  The others are non-spacing.  If those
 * (spacing and nonspacing) do not fill the array, a null L'\0' follows. 
 * Otherwise, a null is assumed to follow when extracting via getcchar().
 */
#define CCHARW_MAX	5

typedef unsigned long chtype;
typedef	chtype	attr_t;		/* ...must be at least as wide as chtype */
/*
 * NCURSES_ATTR_T is used to quiet compiler warnings when building ncurses
 * configured using --disable-macros.
 */
#ifdef NCURSES_NOMACROS
#ifndef NCURSES_ATTR_T
#define NCURSES_ATTR_T attr_t
#endif
#endif /* NCURSES_NOMACROS */

typedef struct
{
    attr_t	attr;
    wchar_t	chars[CCHARW_MAX];
#if 0
#undef NCURSES_EXT_COLORS
#define NCURSES_EXT_COLORS 20110404
    int		ext_color;	/* color pair, must be more than 16-bits */
#endif
}
cchar_t;
//End of added code for ANDROID
typedef struct WINDOW_s {
	int w;
} WINDOW;
extern WINDOW* stdscr;

#define ERR 1
#define getyx(w, y, x)     (y = getcury(w), x = getcurx(w))

int attrset(int);
int wattrset(WINDOW*, int);
int attrget(int, int);
int wattrget(WINDOW*, int, int);
int addch(const char);
int delch();
int waddch(WINDOW*, const char);
int addstr(const char *);
int waddstr(WINDOW*, const char *);
int addnstr(int, const char *);
int waddnstr(WINDOW*, int, const char *);
int move(int, int);
int mvaddch(int, int, const char);
int mvaddstr(int, int, const char *);
int whline(WINDOW*, const char, int);
int hline(const char, int);
int wclrtobot(WINDOW*);
int clrtobot(void);
int clrtoeol(void);
int wclrtoeol(WINDOW*);
#ifndef NO_CLEAR
int clear(void);
#endif
int wclear(WINDOW*);
int initscr(void);
int curs_set(int);
WINDOW* newwin(int,int,int,int);
int getcurx(WINDOW *);
int getcury(WINDOW *);
int overwrite(const WINDOW *, WINDOW *);
int touchwin(WINDOW *);
int delwin(WINDOW *);
int refresh(void);
int mvinch(int, int);
int mvwinch(WINDOW*, int, int);
int crmode();
int nonl();
int noecho();
int nl();
int echo();
int cbreak();
int nocbreak();
int notimeout(WINDOW *, int);
int endwin();
int has_colors();
int start_color();
int scrollok(WINDOW *, int); 
int scroll(WINDOW *); 
int intrflush(WINDOW *, int);
int beep(void);
int keypad(WINDOW *, int); 
int init_color(int, int);
int init_pair(int, int, int); 

int crawl_getch(int v);
int flushinp(void);
int noise(void);

void crawl_quit(const char*);
void crawl_warn(const char*);

#ifdef USE_MY_STR
size_t my_strcpy(char *, const char *, size_t);
size_t my_strcat(char *, const char *, size_t);
#endif

/* game must implement these */
void crawl_process_argv(int, const char*);
void crawl_main(void);
int queryInt(const char* argv0);

/* game may implement these */
extern void (*crawl_quit_hook)(void);
