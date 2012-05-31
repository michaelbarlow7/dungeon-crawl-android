# -*- Makefile -*- for sdl-image

ifneq ($(findstring $(MAKEFLAGS),s),s)
ifndef V
        QUIET_CC       = @echo '   ' CC $@;
        QUIET_AR       = @echo '   ' AR $@;
        QUIET_RANLIB   = @echo '   ' RANLIB $@;
        QUIET_INSTALL  = @echo '   ' INSTALL $<;
        export V
endif
endif

LIB    = libSDL_image.a
AR    ?= ar
CC    ?= gcc
RANLIB?= ranlib
RM    ?= rm -f

prefix ?= /usr/local
libdir := $(prefix)/lib
includedir := $(prefix)/include/SDL

HEADERS = SDL_image.h
SOURCES = IMG.c IMG_bmp.c IMG_gif.c IMG_ImageIO.c IMG_jpg.c IMG_lbm.c \
    IMG_pcx.c IMG_png.c IMG_pnm.c IMG_tga.c IMG_tif.c IMG_xcf.c \
	IMG_xpm.c IMG_xv.c

HEADERS_INST := $(patsubst %,$(includedir)/%,$(HEADERS))
OBJECTS := $(patsubst %.c,%.o,$(SOURCES))

CFLAGS ?= -O2
CFLAGS += -I$(prefix)/include -I$(includedir) -DLOAD_PNG -DLOAD_BMP

.PHONY: install

all: $(LIB)

$(includedir)/%.h: %.h
	-@if [ ! -d $(includedir)  ]; then mkdir -p $(includedir); fi
	$(QUIET_INSTALL)cp $< $@
	@chmod 0644 $@

$(libdir)/%.a: %.a
	-@if [ ! -d $(libdir)  ]; then mkdir -p $(libdir); fi
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
        echo "    * rebuilding sdl-image: new build flags or prefix"; \
        echo "$$FLAGS" > .cflags; \
    fi

.PHONY: .force-cflags
