# -*- Makefile -*- for PCRE (Win32, MinGW)

ifneq ($(findstring $(MAKEFLAGS),s),s)
ifndef V
        QUIET_CC       = @echo '   ' CC $@;
        QUIET_AR       = @echo '   ' AR $@;
        QUIET_RANLIB   = @echo '   ' RANLIB $@;
        QUIET_INSTALL  = @echo '   ' INSTALL $<;
        export V
endif
endif

prefix ?= /usr/local
libdir := $(prefix)/lib
includedir := $(prefix)/include
mandir := $(prefix)/share/man
man3dir := $(mandir)/man3

MAN3 := \
	doc/pcre.3 \
	doc/pcre_compile.3 \
	doc/pcre_compile2.3 \
	doc/pcre_config.3 \
	doc/pcre_copy_named_substring.3 \
	doc/pcre_copy_substring.3 \
	doc/pcre_dfa_exec.3 \
	doc/pcre_exec.3 \
	doc/pcre_free_substring.3 \
	doc/pcre_free_substring_list.3 \
	doc/pcre_fullinfo.3 \
	doc/pcre_get_named_substring.3 \
	doc/pcre_get_stringnumber.3 \
	doc/pcre_get_stringtable_entries.3 \
	doc/pcre_get_substring.3 \
	doc/pcre_get_substring_list.3 \
	doc/pcre_info.3 \
	doc/pcre_maketables.3 \
	doc/pcre_refcount.3 \
	doc/pcre_study.3 \
	doc/pcre_version.3 \
	doc/pcreapi.3 \
	doc/pcrebuild.3 \
	doc/pcrecallout.3 \
	doc/pcrecompat.3 \
	doc/pcrematching.3 \
	doc/pcrepartial.3 \
	doc/pcrepattern.3 \
	doc/pcreperform.3 \
	doc/pcreposix.3 \
	doc/pcreprecompile.3 \
	doc/pcresample.3 \
	doc/pcrestack.3 \
	doc/pcresyntax.3

MAN3_INST := $(patsubst doc/%,$(man3dir)/%,$(MAN3))

OBJECTS := \
	pcre_chartables.o \
	pcre_compile.o \
	pcre_config.o \
	pcre_dfa_exec.o \
	pcre_exec.o \
	pcre_fullinfo.o \
	pcre_get.o \
	pcre_globals.o \
	pcre_info.o \
	pcre_maketables.o \
	pcre_newline.o \
	pcre_ord2utf8.o \
	pcre_refcount.o \
	pcre_study.o \
	pcre_tables.o \
	pcre_try_flipped.o \
	pcre_ucd.o \
	pcre_valid_utf8.o \
	pcre_version.o \
	pcre_xclass.o

CC ?= gcc
AR ?= ar
RANLIB ?= ranlib
RM ?= rm -f

CFLAGS += -I. -DHAVE_CONFIG_H
LDFLAGS :=

LIBNAME := libpcre.a

all: $(LIBNAME)

distclean: clean

clean:
	$(RM) $(LIBNAME) $(OBJECTS) .cflags

$(LIBNAME) : $(OBJECTS)
	@$(RM) $@
	$(QUIET_AR)$(AR) rcu $@ $?
	$(QUIET_RANLIB)$(RANLIB) $@

%.o: %.c .cflags
	$(QUIET_CC)$(CC) $(CFLAGS) -o $@ -c $<

$(includedir)/%.h: %.h
	-@if [ ! -d $(includedir)  ]; then mkdir -p $(includedir); fi
	$(QUIET_INSTALL)cp $< $@
	@chmod 0644 $@

$(man3dir)/%.3: doc/%.3
	-@if [ ! -d $(man3dir)  ]; then mkdir -p $(man3dir); fi
	$(QUIET_INSTALL)cp $< $@
	@chmod 0644 $@

$(libdir)/%.a: %.a
	-@if [ ! -d $(libdir)  ]; then mkdir -p $(libdir); fi
	$(QUIET_INSTALL)cp $< $@
	@chmod 0755 $@

install: $(libdir)/$(LIBNAME) $(MAN3_INST)  $(includedir)/pcre.h

TRACK_CFLAGS = $(subst ','\'',$(CC) $(CFLAGS))

.cflags: .force-cflags
	@FLAGS='$(TRACK_CFLAGS)'; \
    if test x"$$FLAGS" != x"`cat .cflags 2>/dev/null`" ; then \
        echo "    * rebuilding pcre: new build flags or prefix"; \
        echo "$$FLAGS" > .cflags; \
    fi

.PHONY: .force-cflags
