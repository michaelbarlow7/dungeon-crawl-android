# -*- Makefile -*- for stripped down SQLite 3 static lib.

ifneq ($(findstring $(MAKEFLAGS),s),s)
ifndef V
        QUIET_CC       = @echo '   ' CC $@;
        QUIET_AR       = @echo '   ' AR $@;
        QUIET_RANLIB   = @echo '   ' RANLIB $@;
        QUIET_INSTALL  = @echo '   ' INSTALL $<;
        export V
endif
endif

LIBSQL = libsqlite3.a
AR    ?= ar
CC    ?= gcc
RANLIB?= ranlib
RM    ?= rm -f

prefix ?= /usr/local
libdir := $(prefix)/lib
includedir := $(prefix)/include

# Omit SQLite features we don't need.
CFLAGS ?= -O2
CFLAGS +=-DSQLITE_OMIT_AUTHORIZATION \
		 -DSQLITE_OMIT_AUTOVACUUM \
		 -DSQLITE_OMIT_COMPLETE \
		 -DSQLITE_OMIT_BLOB_LITERAL \
		 -DSQLITE_OMIT_COMPOUND_SELECT \
		 -DSQLITE_OMIT_CONFLICT_CLAUSE \
		 -DSQLITE_OMIT_DATETIME_FUNCS \
		 -DSQLITE_OMIT_EXPLAIN \
		 -DSQLITE_OMIT_INTEGRITY_CHECK \
		 -DSQLITE_OMIT_PAGER_PRAGMAS \
		 -DSQLITE_OMIT_PROGRESS_CALLBACK \
		 -DSQLITE_OMIT_SCHEMA_PRAGMAS \
		 -DSQLITE_OMIT_SCHEMA_VERSION_PRAGMAS \
		 -DSQLITE_OMIT_TCL_VARIABLE \
		 -DSQLITE_OMIT_LOAD_EXTENSION \
		 -DSQLITE_DEBUG=0 \
		 -w
ifeq ($(shell uname -s),Darwin)
  CFLAGS += -DSQLITE_ENABLE_LOCKING_STYLE=0
endif

.PHONY: install

all: $(LIBSQL)

$(includedir)/%.h: %.h
	-@if [ ! -d $(includedir)  ]; then mkdir -p $(includedir); fi
	$(QUIET_INSTALL)cp $< $@
	@chmod 0644 $@

$(libdir)/%.a: %.a
	-@if [ ! -d $(libdir)  ]; then mkdir -p $(libdir); fi
	$(QUIET_INSTALL)cp $< $@
	@chmod 0644 $@

install: $(includedir)/sqlite3.h $(libdir)/libsqlite3.a

clean:
	$(RM) sqlite3.o $(LIBSQL) .cflags

distclean: clean

$(LIBSQL): sqlite3.o
	@$(RM) $@
	$(QUIET_AR)$(AR) rcu $@ $^
	$(QUIET_RANLIB)$(RANLIB) $@

%.o: %.c .cflags
	$(QUIET_CC)$(CC) $(CFLAGS) -o $@ -c $<

TRACK_CFLAGS = $(subst ','\'',$(CC) $(CFLAGS))

.cflags: .force-cflags
	@FLAGS='$(TRACK_CFLAGS)'; \
    if test x"$$FLAGS" != x"`cat .cflags 2>/dev/null`" ; then \
        echo "    * rebuilding sqlite: new build flags or prefix"; \
        echo "$$FLAGS" > .cflags; \
    fi

.PHONY: .force-cflags
