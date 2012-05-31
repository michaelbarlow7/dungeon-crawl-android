# -*- Makefile -*- for lua

ifneq ($(findstring $(MAKEFLAGS),s),s)
ifndef V
        QUIET_CC       = @echo '   ' CC $@;
        QUIET_AR       = @echo '   ' AR $@;
        QUIET_RANLIB   = @echo '   ' RANLIB $@;
        QUIET_INSTALL  = @echo '   ' INSTALL $<;
        export V
endif
endif

LIB    = liblua.a
AR    ?= ar
CC    ?= gcc
RANLIB?= ranlib
RM    ?= rm -f

prefix ?= /usr/local
libdir := $(prefix)/lib
includedir := $(prefix)/include

HEADERS := lua.h luaconf.h lualib.h lauxlib.h
SOURCES = \
    lapi.c \
    lcode.c \
    ldebug.c \
    ldo.c \
    ldump.c \
    lfunc.c \
    lgc.c \
    llex.c \
    lmem.c \
    lobject.c \
    lopcodes.c \
    lparser.c \
    lstate.c \
    lstring.c \
    ltable.c \
    ltm.c \
    lundump.c \
    lvm.c \
    lzio.c \
    lauxlib.c \
    lbaselib.c \
    ldblib.c \
    liolib.c \
    lmathlib.c \
    loslib.c \
    ltablib.c \
    lstrlib.c \
    loadlib.c \
    linit.c

SOURCES := $(shell echo $(SOURCES))
HEADERS_INST := $(patsubst %,$(includedir)/%,$(HEADERS))
OBJECTS := $(patsubst %.c,%.o,$(SOURCES))

CFLAGS ?= -O2

.PHONY: install

all: $(LIB)

$(includedir)/%.h: %.h
	@mkdir -p $(includedir)
	$(QUIET_INSTALL)cp $< $@
	@chmod 0644 $@

$(libdir)/%.a: %.a
	@mkdir -p $(libdir)
	$(QUIET_INSTALL)cp $< $@
	@chmod 0644 $@

install: $(HEADERS_INST) $(libdir)/$(LIB)

clean:
	$(RM) $(OBJECTS) $(LIB) .cflags

distclean: clean

$(LIB): $(OBJECTS)
	@$(RM) $@
	$(QUIET_AR)$(AR) rcu $@ $^
	$(QUIET_RANLIB)$(RANLIB) $@

%.o: %.c .cflags
	$(QUIET_CC)$(CC) $(CFLAGS) -o $@ -c $<

TRACK_CFLAGS = $(subst ','\'',$(CC) $(CFLAGS))

.cflags: .force-cflags
	@FLAGS='$(TRACK_CFLAGS)'; \
    if test x"$$FLAGS" != x"`cat .cflags 2>/dev/null`" ; then \
        echo "    * rebuilding lua: new build flags or prefix"; \
        echo "$$FLAGS" > .cflags; \
    fi

.PHONY: .force-cflags
