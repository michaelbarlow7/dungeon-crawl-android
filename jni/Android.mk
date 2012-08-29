LOCAL_PATH := $(call my-dir)

#####################################################################
# build sqlite3                                          
# as per instructions here http://www.roman10.net/how-to-compile-sqlite-for-android-using-ndk/
#####################################################################
include $(CLEAR_VARS)
SQLITE_DIR := contrib/sqlite
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(SQLITE_DIR)
LOCAL_MODULE:=sqlite3
LOCAL_SRC_FILES:=$(SQLITE_DIR)/sqlite3.c
include $(BUILD_STATIC_LIBRARY)
#####################################################################
# build lua	
#####################################################################
include $(CLEAR_VARS)
LUA_DIR := contrib/lua/src
LOCAL_ARM_MODE  := arm
LOCAL_MODULE    := lua
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(LUA_DIR)
LOCAL_SRC_FILES := $(LUA_DIR)/lapi.c \
	$(LUA_DIR)/lauxlib.c \
	$(LUA_DIR)/lbaselib.c \
	$(LUA_DIR)/lcode.c \
	$(LUA_DIR)/ldblib.c \
	$(LUA_DIR)/ldebug.c \
	$(LUA_DIR)/ldo.c \
	$(LUA_DIR)/ldump.c \
	$(LUA_DIR)/lfunc.c \
	$(LUA_DIR)/lgc.c \
	$(LUA_DIR)/linit.c \
	$(LUA_DIR)/liolib.c \
	$(LUA_DIR)/llex.c \
	$(LUA_DIR)/lmathlib.c \
	$(LUA_DIR)/lmem.c \
	$(LUA_DIR)/loadlib.c \
	$(LUA_DIR)/lobject.c \
	$(LUA_DIR)/lopcodes.c \
	$(LUA_DIR)/loslib.c \
	$(LUA_DIR)/lparser.c \
	$(LUA_DIR)/lstate.c \
	$(LUA_DIR)/lstring.c \
	$(LUA_DIR)/lstrlib.c \
	$(LUA_DIR)/ltable.c \
	$(LUA_DIR)/ltablib.c \
	$(LUA_DIR)/ltm.c \
	$(LUA_DIR)/lundump.c \
	$(LUA_DIR)/lvm.c \
	$(LUA_DIR)/lzio.c \
	$(LUA_DIR)/print.c
 
include $(BUILD_STATIC_LIBRARY)
#include $(BUILD_SHARED_LIBRARY)

#####################################################################
#            start building app                                     #
#####################################################################
include $(CLEAR_VARS)
RLTILES_DIR := rltiles

# Crawl files are in .cc format
LOCAL_CPP_EXTENSION=.cc
# Need to include the rtiles folder, sqlite, lua and curses
LOCAL_C_INCLUDES := $(LOCAL_PATH)/rltiles \
$(LOCAL_PATH)/rltiles/tool \
$(LOCAL_PATH)/prebuilt \
$(LOCAL_PATH)/$(SQLITE_DIR) \
$(LOCAL_PATH)/$(LUA_DIR) \
$(LOCAL_PATH)/curses
# Loading the sqlite and lua libraries
LOCAL_STATIC_LIBRARIES:=libsqlite3 liblua
# loading zlib and logging functions
LOCAL_LDLIBS := -lz -llog

# These flags are on by default in the original Makefile
LOCAL_CFLAGS += -DCLUA_BINDINGS -DWIZARD

# This is basically all the .cc files in the 'source' folder
CRAWLSRC = abl-show.cc abyss.cc acquire.cc act-iter.cc actor.cc actor-los.cc \
AppHdr.cc areas.cc arena.cc artefact.cc asg.cc attack.cc attitude-change.cc beam.cc \
behold.cc bitary.cc branch.cc cellular.cc chardump.cc cio.cc cloud.cc clua.cc cluautil.cc \
colour.cc command.cc coord.cc coord-circle.cc coordit.cc crash.cc ctest.cc dactions.cc \
database.cc dbg-asrt.cc dbg-maps.cc dbg-scan.cc dbg-util.cc decks.cc delay.cc describe.cc \
dgl-message.cc dgn-delve.cc dgnevent.cc dgn-height.cc dgn-labyrinth.cc dgn-layouts.cc \
dgn-overview.cc dgn-shoals.cc dgn-swamp.cc directn.cc dlua.cc dungeon.cc effects.cc \
errors.cc evoke.cc exclude.cc exercise.cc fearmonger.cc feature.cc fight.cc files.cc \
fineff.cc fontwrapper-ft.cc food.cc format.cc fprop.cc geom2d.cc ghost.cc glwrapper.cc \
glwrapper-ogl.cc godabil.cc godconduct.cc goditem.cc godmenu.cc godpassive.cc \
godprayer.cc godwrath.cc hints.cc hiscores.cc initfile.cc invent.cc itemname.cc \
itemprop.cc items.cc item_use.cc jobs.cc json.cc kills.cc l_colour.cc l_crawl.cc \
l_debug.cc l_dgnbld.cc l_dgn.cc l_dgnevt.cc l_dgngrd.cc l_dgnit.cc l_dgnlvl.cc \
l_dgnmon.cc l_dgntil.cc lev-pand.cc l_feat.cc l_file.cc l_food.cc l_global.cc \
libgui.cc libutil.cc libandroid.cc libw32c.cc l_item.cc l_los.cc l_mapgrd.cc \
l_mapmrk.cc l_moninf.cc l_mons.cc l_option.cc los.cc los_def.cc losglobal.cc \
losparam.cc l_spells.cc l_subvault.cc l_travel.cc luaterp.cc l_view.cc \
l_you.cc macro.cc main.cc makeitem.cc mapdef.cc map_knowledge.cc mapmark.cc \
maps.cc melee_attack.cc menu.cc message.cc message-stream.cc misc.cc \
mislead.cc mon-abil.cc mon-act.cc mon-behv.cc mon-cast.cc mon-clone.cc \
mon-death.cc mon-ench.cc mon-gear.cc mon-grow.cc mon-info.cc mon-iter.cc \
mon-movetarget.cc mon-pathfind.cc mon-pick.cc mon-place.cc mon-project.cc \
mon_resist_def.cc mon-speak.cc mon-stealth.cc monster.cc mon-stuff.cc mon-transit.cc \
mon-util.cc mutation.cc newgame.cc ng-init.cc ng-input.cc ng-restr.cc ng-setup.cc \
ng-wanderer.cc notes.cc orb.cc ouch.cc output.cc package.cc pattern.cc \
place.cc place-info.cc player-act.cc player.cc player-equip.cc player-stats.cc \
potion.cc quiver.cc random.cc random-var.cc ray.cc religion.cc rng.cc shopping.cc \
shout.cc show.cc showsymb.cc skill_menu.cc skills2.cc skills.cc species.cc spl-book.cc \
spl-cast.cc spl-clouds.cc spl-damage.cc spl-goditem.cc spl-miscast.cc spl-monench.cc \
spl-other.cc spl-selfench.cc spl-summoning.cc spl-tornado.cc spl-transloc.cc \
spl-util.cc spl-wpnench.cc spl-zap.cc sprint.cc sqldbm.cc stairs.cc startup.cc \
stash.cc state.cc status.cc store.cc stuff.cc syscalls.cc tags.cc tagstring.cc \
target.cc teleport.cc terrain.cc tilebuf.cc tilefont.cc tilepick.cc \
tiletex.cc tileview.cc tileweb.cc \
transform.cc traps.cc travel.cc tutorial.cc unicode.cc version.cc view.cc \
viewchar.cc viewgeom.cc viewmap.cc wcwidth.cc windowmanager-sdl.cc wiz-dgn.cc \
wiz-fsim.cc wiz-item.cc wiz-mon.cc wiz-you.cc xom.cc zotdef.cc \
\
$(RLTILES_DIR)/tiledef-dngn.cc  $(RLTILES_DIR)/tiledef-feat.cc  $(RLTILES_DIR)/tiledef-floor.cc  \
$(RLTILES_DIR)/tiledef-gui.cc  $(RLTILES_DIR)/tiledef-icons.cc  $(RLTILES_DIR)/tiledef-main.cc  \
$(RLTILES_DIR)/tiledef-player.cc  $(RLTILES_DIR)/tiledef-unrand.cc  $(RLTILES_DIR)/tiledef-wall.cc \
\
prebuilt/levcomp.lex.cc prebuilt/levcomp.tab.cc \

#Removed from above
# libunix.cc changed to libandroid.cc
# tilecell.cc tiledgnbuf.cc
# tiledoll.cc tilemcache.cc tilepick-p.cc tilereg.cc tilereg-cmd.cc\
# tilereg-crt.cc tilereg-dgn.cc tilereg-doll.cc tilereg-grid.cc tilereg-inv.cc \
# tilereg-map.cc tilereg-mem.cc tilereg-menu.cc tilereg-mon.cc tilereg-msg.cc \
# tilereg-skl.cc tilereg-spl.cc tilereg-stat.cc tilereg-tab.cc tilereg-text.cc tilereg-grid.cc\
# tilereg-title.cc tilesdl.cc  tileweb-text.cc \

LOCAL_MODULE    := crawl

LOCAL_SRC_FILES := $(CRAWLSRC)

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_EXECUTABLE)
