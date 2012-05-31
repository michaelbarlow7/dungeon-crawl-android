# -*- Makefile -*- for freetype

ifneq ($(findstring $(MAKEFLAGS),s),s)
ifndef V
        QUIET_CC       = @echo '   ' CC $@;
        QUIET_AR       = @echo '   ' AR $@;
        QUIET_RANLIB   = @echo '   ' RANLIB $@;
        QUIET_INSTALL  = @echo '   ' INSTALL $<;
        export V
endif
endif

uname_S := $(shell uname -s)

# Since Windows builds could be done with MinGW or Cygwin,
# set a TARGET_OS_WINDOWS flag when either shows up.
ifneq (,$(findstring MINGW,$(uname_S)))
TARGET_OS_WINDOWS := YesPlease
endif
ifneq (,$(findstring CYGWIN,$(uname_S)))
TARGET_OS_WINDOWS := YesPlease
endif

LIB    = libfreetype.a
AR    ?= ar
CC    ?= gcc
RANLIB?= ranlib
RM    ?= rm -f

prefix ?= /usr/local
libdir := $(prefix)/lib
includedir := $(prefix)/include/freetype2

HEADERS := $(shell find include -type f -name '*.h')
SOURCES = \
    src/base/ftbase.c \
    src/base/ftbbox.c \
	src/base/ftbdf.c \
	src/base/ftbitmap.c \
    src/base/ftdebug.c \
	src/base/ftfstype.c \
    src/base/ftglyph.c \
	src/base/ftgasp.c \
    src/base/ftinit.c \
    src/base/ftsystem.c \
	src/autofit/autofit.c \
	src/cache/ftcache.c \
	src/sfnt/sfnt.c \
	src/smooth/ftgrays.c \
	src/smooth/ftsmooth.c \
	src/truetype/truetype.c

SOURCES := $(shell echo $(SOURCES))
HEADERS_INST := $(patsubst include/%,$(includedir)/%,$(HEADERS))
OBJECTS := $(patsubst %.c,%.o,$(SOURCES))

CFLAGS ?= -O2
CFLAGS += -Iinclude -DFT2_BUILD_LIBRARY

.PHONY: install

all: $(LIB)

$(includedir)/%.h: include/%.h
	@mkdir -p $(includedir)/$(shell dirname $(patsubst include/%,%,$<))
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
        echo "    * rebuilding freetype: new build flags or prefix"; \
        echo "$$FLAGS" > .cflags; \
    fi

.PHONY: .force-cflags

