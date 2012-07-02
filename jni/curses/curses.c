#include "curses.h"

#define JAVA_CALL(...) ((*env)->CallVoidMethod(env, NativeWrapperObj, __VA_ARGS__))
#define JAVA_CALL_INT(...) ((*env)->CallIntMethod(env, NativeWrapperObj, __VA_ARGS__))
#define JAVA_METHOD(m,s) ((*env)->GetMethodID(env, NativeWrapperClass, m, s))

#define WIN_MAX 100
WINDOW _win[WIN_MAX];
WINDOW* stdscr = &_win[0];

//#define LOGC(...) 
#define LOGC(...) __android_log_print(ANDROID_LOG_DEBUG  , "Crawl", __VA_ARGS__)

static jmp_buf jbuf;

/* JVM enviroment */
static JavaVM *jvm;
static JNIEnv *env;

static jclass NativeWrapperClass;
static jobject NativeWrapperObj;

/* Java Methods */
//~ static jmethodID NativeWrapper_fatal;
//~ static jmethodID NativeWrapper_warn;
//~ static jmethodID NativeWrapper_waddnstr;
//~ static jmethodID NativeWrapper_wattrset;
//~ static jmethodID NativeWrapper_wattrget;
//~ static jmethodID NativeWrapper_overwrite;
//~ static jmethodID NativeWrapper_touchwin;
//~ static jmethodID NativeWrapper_whline;
//~ static jmethodID NativeWrapper_wclear;
//~ static jmethodID NativeWrapper_wclrtoeol;
//~ static jmethodID NativeWrapper_wclrtobot;
//~ static jmethodID NativeWrapper_noise;
//~ static jmethodID NativeWrapper_init_color;
//~ static jmethodID NativeWrapper_init_pair;
//~ static jmethodID NativeWrapper_initscr;
//~ static jmethodID NativeWrapper_newwin;
//~ static jmethodID NativeWrapper_delwin;
//~ static jmethodID NativeWrapper_scroll;
//~ static jmethodID NativeWrapper_wrefresh;
//~ static jmethodID NativeWrapper_getch;
//~ static jmethodID NativeWrapper_wmove;
//~ static jmethodID NativeWrapper_mvwinch;
//~ static jmethodID NativeWrapper_curs_set;
//~ static jmethodID NativeWrapper_flushinp;
//~ static jmethodID NativeWrapper_getcury;
//~ static jmethodID NativeWrapper_getcurx;
//~ // #ifdef ANGDROID_NIGHTLY
//~ static jmethodID NativeWrapper_wctomb;
//~ static jmethodID NativeWrapper_mbstowcs;
//~ static jmethodID NativeWrapper_wcstombs;
//~ // #endif
//~ static jmethodID NativeWrapper_score_start;
//~ static jmethodID NativeWrapper_score_detail;
//~ static jmethodID NativeWrapper_score_submit;
static int myXPos = 0;
static int myYPos = 0;

void (*crawl_quit_hook)(void) = NULL;

int attrset(int attrs) {
	LOGC("curses.attrset %d",attrs);
	return wattrset(stdscr, attrs);
}
int wattrset(WINDOW* w, int attrs) {
	LOGC("curses.wattrset %d %d",w->w,attrs);
	//~ JAVA_CALL(NativeWrapper_wattrset, w->w, attrs);
	return 0;
}
int attrget(int row, int col) {
	return wattrget(stdscr,row,col);
}
int wattrget(WINDOW* w,int row, int col) {
	LOGC("curses.wattrget %d %d %d",w->w,row,col);
	//~ int attrs = JAVA_CALL_INT(NativeWrapper_wattrget, w->w, row, col);
	int attrs = 0; //ANDROID
	return attrs;
}

int addch(const char a){
	addnstr(1,&a);
	return 0;
}
int delch(){
	int x,y;
	getyx(stdscr, y, x);
	mvaddch(y, x-1, ' ');
	move(y, x-1);
	return 0;
}

int waddch(WINDOW *w, const char a){
	waddnstr(w,1,&a);
	return 0;
}

int addstr(const char *s){
	addnstr(strlen(s), s);
	return 0;
}
int waddstr(WINDOW * w, const char *s){
	waddnstr(w,strlen(s), s);
	return 0;
}

int addnstr(int n, const char *s) {
	waddnstr(stdscr, n, s);
}

int waddnstr(WINDOW* w, int n, const char *s) {
	//~ jbyteArray array = (*env)->NewByteArray(env, n);
	//~ if (array == NULL) crawl_quit("Error: Out of memory");
	//~ (*env)->SetByteArrayRegion(env, array, 0, n, s);
	LOGC("curses.waddnstr %d %d %c",w->w,n,s[0]);
	//~ JAVA_CALL(NativeWrapper_waddnstr, w->w, n, array);
	//~ (*env)->DeleteLocalRef(env, array);
	return 0;
}

int move(int y, int x) {
	LOGC("curses.move %d %d",y,x);
	//~ JAVA_CALL(NativeWrapper_wmove, 0, y, x);
	myYPos = y;
	myXPos = x;
	return 0;
}

int wmove(WINDOW* w, int y, int x) {
	LOGC("curses.wmove %d %d %d",y,x);
	//~ JAVA_CALL(NativeWrapper_wmove, w->w, y, x);
	return 0;
}

int mvaddch(int y, int x, const char a){
	move(y,x);
	addch(a);
	return 0;
}

int mvwaddch(WINDOW * w,int y, int x, const char a){
	wmove(w,y,x);
	waddch(w,a);
	return 0;
}

int mvwaddstr(WINDOW* w,int y, int x, const char *s){
	wmove(w,y,x);
	waddstr(w,s);
	return 0;
}
int mvaddstr(int y, int x, const char *s){
	move(y,x);
	addstr(s);
	return 0;
}

int hline(const char a, int n){
	whline(stdscr, a, n);
	return 0;
}
int whline(WINDOW* w, const char a, int n){
	LOGC("curses.whline %d %c %d",w-w,a,n);
	//~ JAVA_CALL(NativeWrapper_whline, w->w, a, n);
	return 0;
}

int clrtoeol(void){
	wclrtoeol(stdscr);
	return 0;
}
int wclrtoeol(WINDOW *w){
	LOGC("curses.wclrtoeol %d",w->w);
	//~ JAVA_CALL(NativeWrapper_wclrtoeol, w->w);
	return 0;
}

int clear(void){
	wclear(stdscr);
	return 0;
}
int wclear(WINDOW* w){
	LOGC("curses.wclear %d",w->w);
	//~ JAVA_CALL(NativeWrapper_wclear, w->w);
	return 0;
}

int initscr() {
	LOGC("curses.initscr");
	//~ JAVA_CALL(NativeWrapper_initscr);
	stdscr->w = 0;
	clear();
	touchwin(stdscr);
	return 0;
}

int crmode() {
	return 0;
}
int nonl() {
	return 0;
}
int noecho() {
	return 0;
}
int nl() {
	return 0;
}
int echo() {
	return 0;
}
int notimeout(WINDOW *w, int bf) {
	return 0;
}
int endwin() {
	LOGC("curses.endwin");
	//~ JAVA_CALL(NativeWrapper_initscr);
	return 0;
}
int cbreak() {
	return 0;
}
int nocbreak() {
	return 0;
}
int scrollok(WINDOW *w, int bf){
	return 0;
}
int scroll(WINDOW *w) {
	LOGC("curses.scroll %d",w->w);
	//~ JAVA_CALL(NativeWrapper_scroll, w->w);
	return 0;
}

int has_colors() {
	return -1;
}
int start_color() {
	int colors = 16;

	int color_table[] = {
		0xFF000000, //BLACK
		0xFF0040FF, //BLUE
		0xFF008040, //GREEN
		0xFF00A0A0, //CYAN
		0xFFFF4040, //RED
		0xFF9020FF, //MAGENTA
		0xFFFFFF00, //YELLOW
		0xFFC0C0C0, //WHITE
		0xFF606060, //BRIGHT_BLACK (GRAY)
		0xFF00FFFF, //BRIGHT_BLUE
		0xFF00FF00, //BRIGHT_GREEN
		0xFF20FFDC, //BRIGHT_CYAN
		0xFFFF5050, //BRIGHT_RED
		0xFFB8A8FF, //BRIGHT_MAGENTA
		0xFFFFFF90, //BRIGHT_YELLOW
		0xFFFFFFFF  //BRIGHT_WHITE
	};

	int i;
	for(i=0; i<colors; i++)
		init_color(i, color_table[i]);
	for(i=0; i<colors; i++)
		init_pair(i, i, 0);

	return 0;
}

int intrflush(WINDOW* w, int bf) {
	return 0;
}
int keypad(WINDOW* w, int bf) {
	return 0;
}
int init_color(int c, int rgb) {
	LOGC("curses.init_color %d %d",c,rgb);
	//~ JAVA_CALL(NativeWrapper_init_color, c, rgb);
	return 0;
} 
int init_pair(int pair, int f, int b){
	LOGC("curses.init_pair %d %d %d",pair,f,b);
	//~ JAVA_CALL(NativeWrapper_init_pair, pair, f, b);
	return 0;
} 

int clrtobot(void){
	wclrtobot(stdscr);
	return 0;
}
int wclrtobot(WINDOW* w){
	LOGC("curses.wclrtobot %d",w->w);
	//~ JAVA_CALL(NativeWrapper_wclrtobot, w->w);
	return 0;
}

int beep() {
	noise();
	return 0;
}

int getcurx(WINDOW *w){
	LOGC("curses.getcurx %d",w->w);
	//~ return JAVA_CALL_INT(NativeWrapper_getcurx, w->w);
	return myXPos; //ANDROID
}

int getcury(WINDOW *w){
	LOGC("curses.getcury %d",w->w);
	//~ return JAVA_CALL_INT(NativeWrapper_getcury, w->w);
	return myYPos; //ANDROID
}

int curs_set(int v) {
	LOGC("curses.curs_set %d",v);
	//~ JAVA_CALL(NativeWrapper_curs_set, v);
}

WINDOW* newwin(int rows, int cols, 
			   int begin_y, int begin_x) {
	LOGC("curses.newwin %d %d %d %d",rows,cols,begin_y,begin_x);
	//~ int k = JAVA_CALL_INT(NativeWrapper_newwin, rows, cols, begin_y, begin_x);
	int k = 0;

	//hack
	WINDOW* ret = stdscr;
	if (k<WIN_MAX) {
		_win[k].w = k;
		ret = &_win[k];
	}
	return ret;
}
int delwin(WINDOW* w) {
	LOGC("curses.delwin %d",w->w);
	//~ JAVA_CALL(NativeWrapper_delwin, w->w);
	return 0;
}

int overwrite(const WINDOW *src, WINDOW *dst){
	LOGC("curses.overwrite %d %d",src->w,dst->w);
	//~ JAVA_CALL(NativeWrapper_overwrite, src->w, dst->w);
	return 0;
}

int touchwin(WINDOW *w){
	LOGC("curses.touchwin %d",w->w);
	//~ JAVA_CALL(NativeWrapper_touchwin, w->w);
	return 0;
}

int wrefresh(WINDOW *w){
	LOGC("curses.wrefresh %d",w->w);
	//~ JAVA_CALL(NativeWrapper_wrefresh, w->w);
	return 0;
}

int refresh(void){
	wrefresh(stdscr);
	return 0;
}

int crawl_getch(int v) {	
	LOGC("curses.getch %d",v);
	//~ int k = JAVA_CALL_INT(NativeWrapper_getch, v);
	int k = 0;
	return k;
}

int mvinch(int r, int c) {
	int ch = mvwinch(stdscr,r,c);
	return ch;
}
int mvwinch(WINDOW* w, int r, int c) {
	LOGC("curses.mvwinch %d %d %d",w->w,r,c);
	//~ int ch = JAVA_CALL_INT(NativeWrapper_mvwinch, w->w, r, c);
	int ch = 0;
	return ch;
}

int flushinp() {
	LOGC("curses.flushinp");
	//~ JAVA_CALL(NativeWrapper_flushinp);
	return 0;
}

int noise() {
	LOGC("curses.noise");
	//~ JAVA_CALL(NativeWrapper_noise);
	return 0;
}

void crawl_quit(const char* msg) {
	if (msg) {
		LOGE(msg);
		//~ JAVA_CALL(NativeWrapper_fatal, (*env)->NewStringUTF(env, msg));
	}

	if (crawl_quit_hook){
		(*crawl_quit_hook)();
	}

	longjmp(jbuf,1);
}

void crawl_warn(const char* msg) {
	if (msg) {
		LOGW(msg);
		//~ JAVA_CALL(NativeWrapper_warn, (*env)->NewStringUTF(env, msg));
	}
}


// #if defined(ANGDROID_NIGHTLY)

int wctomb(char *pmb, wchar_t character) {
	//~ // LOGD("begin wctomb (c)");
	//~ jbyteArray pmb_a = (*env)->NewByteArray(env, 4);
	//~ if (pmb_a == NULL) crawl_quit("wctomb: Out of memory");
	//~ int res = JAVA_CALL_INT(NativeWrapper_wctomb, pmb_a, character);
	//~ (*env)->GetByteArrayRegion(env, pmb_a, 0, res, pmb);
	//~ pmb[res] = 0;
	//~ (*env)->DeleteLocalRef(env, pmb_a);
	//~ // LOGD("end wctomb (c)");
	//~ return res;
	//~ return 0;
}

size_t mbstowcs(wchar_t *wcstr, const char *mbstr, size_t max) {
	//~ //  LOGD("begin mbstowcs (%s,%d)", mbstr, max);
	//~ jbyteArray wcstr_a = (*env)->NewByteArray(env, max);
	//~ if (wcstr_a == NULL) crawl_quit("mbstowcs: Out of memory");
	//~ int mblen = strlen(mbstr);
	//~ jbyteArray mbstr_a = (*env)->NewByteArray(env, mblen);
	//~ if (mbstr_a == NULL) crawl_quit("mbstowcs: Out of memory");
	//~ (*env)->SetByteArrayRegion(env, mbstr_a, 0, mblen, mbstr);
	//~ //  LOGD("mbs = |%s|", mbstr);
	//~ int res = JAVA_CALL_INT(NativeWrapper_mbstowcs, wcstr_a, mbstr_a, max);
	//~ if(wcstr) {
	  //~ (*env)->GetByteArrayRegion(env, wcstr_a, 0, res, wcstr);
	  //~ if(res < max) {
	    //~ wcstr[res] = 0;
	  //~ }
	//~ }
	//~ (*env)->DeleteLocalRef(env, wcstr_a);
	//~ (*env)->DeleteLocalRef(env, mbstr_a);
	//~ //  LOGD("end mbstowcs (c)");
	//~ return res;
	return 0;
}

size_t wcstombs(wchar_t *mbstr, const char *wcstr, size_t max) {
	//~ //  LOGD("begin wcstombs (%s,%d)", wcstr, max);
	//~ jbyteArray mbstr_a = (*env)->NewByteArray(env, max);
	//~ if (mbstr_a == NULL) crawl_quit("wcstombs: Out of memory");
	//~ int wclen = strlen(wcstr);
	//~ jbyteArray wcstr_a = (*env)->NewByteArray(env, wclen);
	//~ if (wcstr_a == NULL) crawl_quit("wcstombs: Out of memory");
	//~ (*env)->SetByteArrayRegion(env, wcstr_a, 0, wclen, wcstr);
	//~ //  LOGD("wcs = |%s|", wcstr);
	//~ int res = JAVA_CALL_INT(NativeWrapper_wcstombs, mbstr_a, wcstr_a, max);
	//~ if(mbstr) {
	  //~ (*env)->GetByteArrayRegion(env, mbstr_a, 0, res, mbstr);
	  //~ if(res < max) {
	    //~ mbstr[res] = 0;
	  //~ }
	//~ }
	//~ (*env)->DeleteLocalRef(env, mbstr_a);
	//~ (*env)->DeleteLocalRef(env, wcstr_a);
	//~ //  LOGD("end wcstombs (c)");
	//~ return res;
	return 0;
}

// #endif

void android_score_start() {
  //~ JAVA_CALL(NativeWrapper_score_start);
}

void android_score_detail(char *name, char *value) {
  //~ /* LOGD("score detail '%s' = '%s'", name, value); */
  //~ jbyteArray name_a = (*env)->NewByteArray(env, strlen(name));
  //~ if (name_a == NULL) crawl_quit("score: Out of memory");
  //~ (*env)->SetByteArrayRegion(env, name_a, 0, strlen(name), name);
  //~ jbyteArray value_a = (*env)->NewByteArray(env, strlen(value));
  //~ if (value_a == NULL) crawl_quit("score: Out of memory");
  //~ (*env)->SetByteArrayRegion(env, value_a, 0, strlen(value), value);
  //~ JAVA_CALL(NativeWrapper_score_detail, name_a, value_a);
  //~ (*env)->DeleteLocalRef(env, name_a);
  //~ (*env)->DeleteLocalRef(env, value_a);
}

void android_score_submit(char *score, char *level) {
  //~ /* LOGD("register score as '%s'", score); */
  //~ /* LOGD("register level as '%s'", level); */
  //~ jbyteArray score_a = (*env)->NewByteArray(env, strlen(score));
  //~ if (score_a == NULL) crawl_quit("score: Out of memory");
  //~ (*env)->SetByteArrayRegion(env, score_a, 0, strlen(score), score);
  //~ jbyteArray level_a = (*env)->NewByteArray(env, strlen(level));
  //~ if (level_a == NULL) crawl_quit("score: Out of memory");
  //~ (*env)->SetByteArrayRegion(env, level_a, 0, strlen(level), level);
  //~ JAVA_CALL(NativeWrapper_score_submit, score_a, level_a);
  //~ (*env)->DeleteLocalRef(env, score_a);
  //~ (*env)->DeleteLocalRef(env, level_a);
}

//~ JNIEXPORT void JNICALL crawl_gameStart
//~ (JNIEnv *env1, jobject obj1, jint argc, jobjectArray argv)
//~ {
	//~ env = env1;
//~ 
	//~ /* Save objects */
	//~ NativeWrapperObj = obj1;
//~ 
	//~ /* Get NativeWrapper class */
	//~ NativeWrapperClass = (*env)->GetObjectClass(env, NativeWrapperObj);
//~ 
	//~ /* NativeWrapper Methods */
	//~ NativeWrapper_fatal = JAVA_METHOD("fatal", "(Ljava/lang/String;)V");	
	//~ NativeWrapper_warn = JAVA_METHOD("warn", "(Ljava/lang/String;)V");
	//~ NativeWrapper_waddnstr = JAVA_METHOD("waddnstr", "(II[B)V");
	//~ NativeWrapper_wattrset = JAVA_METHOD("wattrset", "(II)V");
	//~ NativeWrapper_wattrget = JAVA_METHOD("wattrget", "(III)I");
	//~ NativeWrapper_overwrite = JAVA_METHOD("overwrite", "(II)V");
	//~ NativeWrapper_touchwin = JAVA_METHOD("touchwin", "(I)V");
	//~ NativeWrapper_whline = JAVA_METHOD("whline", "(IBI)V");
	//~ NativeWrapper_wclrtobot = JAVA_METHOD("wclrtobot", "(I)V");
	//~ NativeWrapper_wclrtoeol = JAVA_METHOD("wclrtoeol", "(I)V");
	//~ NativeWrapper_wclear = JAVA_METHOD("wclear", "(I)V");
	//~ NativeWrapper_noise = JAVA_METHOD("noise", "()V");
	//~ NativeWrapper_initscr = JAVA_METHOD("initscr", "()V");
	//~ NativeWrapper_wrefresh = JAVA_METHOD("wrefresh", "(I)V");
	//~ NativeWrapper_getch = JAVA_METHOD("getch", "(I)I");
	//~ NativeWrapper_getcury = JAVA_METHOD("getcury", "(I)I");
	//~ NativeWrapper_getcurx = JAVA_METHOD("getcurx", "(I)I");
	//~ NativeWrapper_init_color = JAVA_METHOD("init_color", "(II)V");
	//~ NativeWrapper_init_pair = JAVA_METHOD("init_pair", "(III)V");
	//~ NativeWrapper_newwin = JAVA_METHOD("newwin", "(IIII)I");
	//~ NativeWrapper_delwin = JAVA_METHOD("delwin", "(I)V");
	//~ NativeWrapper_scroll = JAVA_METHOD("scroll", "(I)V");
	//~ NativeWrapper_wmove = JAVA_METHOD("wmove", "(III)V");
	//~ NativeWrapper_mvwinch = JAVA_METHOD("mvwinch", "(III)I");
	//~ NativeWrapper_curs_set = JAVA_METHOD("curs_set", "(I)V");
	//~ NativeWrapper_flushinp = JAVA_METHOD("flushinp", "()V");
//~ // #ifdef ANGDROID_NIGHTLY
	//~ NativeWrapper_wctomb = JAVA_METHOD("wctomb", "([BB)I");
	//~ NativeWrapper_mbstowcs = JAVA_METHOD("mbstowcs", "([B[BI)I");
	//~ NativeWrapper_wcstombs = JAVA_METHOD("wcstombs", "([B[BI)I");
//~ // #endif
	//~ NativeWrapper_score_start = JAVA_METHOD("score_start", "()V");
	//~ NativeWrapper_score_detail = JAVA_METHOD("score_detail", "([B[B)V");
	//~ NativeWrapper_score_submit = JAVA_METHOD("score_submit", "([B[B)V");
//~ 
	//~ // process argc/argv 
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
//~ }

//~ JNIEXPORT jint JNICALL crawl_gameQueryInt
	//~ (JNIEnv *env1, jobject obj1, jint argc, jobjectArray argv) {
	//~ jint result = -1; // -1 indicates error
//~ 
	//~ // process argc/argv 
	//~ jstring argv0 = NULL;
	//~ int i = 0;
//~ 
	//~ argv0 = (*env1)->GetObjectArrayElement(env1, argv, i);
	//~ const char *copy_argv0 = (*env1)->GetStringUTFChars(env1, argv0, 0);
//~ 
	//~ //result = (jint)queryInt(copy_argv0); ANGDROID STUFF
//~ 
	//~ (*env1)->ReleaseStringUTFChars(env1, argv0, copy_argv0);
//~ 
	//~ return result;
//~ }


//~ JNIEXPORT jstring JNICALL crawl_gameQueryString
	//~ (JNIEnv *env1, jobject obj1, jint argc, jobjectArray argv) {
	//~ return (jstring)0; // null indicates error, i.e. not implemented
//~ }
//~ 
//~ #ifdef USE_MY_STR
//~ /*
 //~ * The my_strcpy() function copies up to 'bufsize'-1 characters from 'src'
 //~ * to 'buf' and NUL-terminates the result.  The 'buf' and 'src' strings may
 //~ * not overlap.
 //~ *
 //~ * my_strcpy() returns strlen(src).  This makes checking for truncation
 //~ * easy.  Example: if (my_strcpy(buf, src, sizeof(buf)) >= sizeof(buf)) ...;
 //~ *
 //~ * This function should be equivalent to the strlcpy() function in BSD.
 //~ */
size_t my_strcpy(char *buf, const char *src, size_t bufsize) {
	size_t len = strlen(src);
	size_t ret = len;

	/* Paranoia */
	if (bufsize == 0) return ret;

	/* Truncate */
	if (len >= bufsize) len = bufsize - 1;

	/* Copy the string and terminate it */
	(void)memcpy(buf, src, len);
	buf[len] = '\0';

	/* Return strlen(src) */
	return ret;
}


/*
 * The my_strcat() tries to append a string to an existing NUL-terminated string.
 * It never writes more characters into the buffer than indicated by 'bufsize' and
 * NUL-terminates the buffer.  The 'buf' and 'src' strings may not overlap.
 *
 * my_strcat() returns strlen(buf) + strlen(src).  This makes checking for
 * truncation easy.  Example:
 * if (my_strcat(buf, src, sizeof(buf)) >= sizeof(buf)) ...;
 *
 * This function should be equivalent to the strlcat() function in BSD.
 */
size_t my_strcat(char *buf, const char *src, size_t bufsize) {
	size_t dlen = strlen(buf);

	/* Is there room left in the buffer? */
	if (dlen < bufsize - 1)
	{
		/* Append as much as possible  */
		return (dlen + my_strcpy(buf + dlen, src, bufsize - dlen));
	}
	else
	{
		/* Return without appending */
		return (dlen + strlen(src));
	}
}
//~ #endif /* USE_MY_STR */



