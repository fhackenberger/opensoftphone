#!/usr/bin/perl
$/ = undef;
$_ = <>;

# Everything before PJ_BEGIN_DECL
s/.*PJ_BEGIN_DECL//msg;
# Everything after PG_END_DECL
s/PJ_END_DECL.*//msg;
# An unwanted typedef
s/typedef struct pjsua_msg_data pjsua_msg_data\;//ms;
print;
