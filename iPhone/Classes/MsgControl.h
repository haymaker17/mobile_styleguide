//
//  MsgControl.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/9/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Msg.h"
#import "DataConstants.h"
#import "ExMsgRespondDelegate.h"

#define POSTLIVE 1

@class RootViewController;

@interface MsgControl : NSObject 
{
	NSMutableDictionary	*dataDict;
	RootViewController	*rootVC;
}

@property (strong, nonatomic) RootViewController	*rootVC;

-(void) init:(RootViewController *)rootViewController;

/*
 * create Message with parameterBag
 * cacheOnly : (@"YES") get information only from the cache and do not ask the server, 
 *              (@"NO") get the information for the server
 * skipCache : (YES) get the information from the server without consulting the cache , 
 *              (NO) get first the information from the cache, than when the "timer expires" get the information from the server
 *                   (using NO will result in generating 2 messages and respondToFoundData will be called twice with the same type of message)
 */

-(NSString *) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache;
-(NSString *) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache Options:(int)msgOptions;

-(NSString *) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc;
-(NSString *) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache Options:(int)msgOptions RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc;

-(Msg*) createMsgOptionalAdd:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc ShouldAdd:(BOOL)shouldAdd;
-(Msg*) createMsgOptionalAdd:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache Options:(int)msgOptions RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc ShouldAdd:(BOOL)shouldAdd;


-(MsgResponder*) newMsgResponder:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache;



-(void) add:(Msg *)msg;

+(BOOL) hasCachedData:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;

-(CacheMetaData*) getCachedData:(Msg *)msg;

-(void) msgDone:(Msg *)msg CameFromCache:(NSString *)fromCache;

-(void)saveCache:(NSString *)msgIdKey RecordKey:(NSString *)recordKey Data:(NSData *)webData;

-(CacheMetaData *)getCacheMetaData:(Msg *)msg;


-(BOOL)shouldRequestNewDataForMessage:(Msg*)msg cacheMetaData:(CacheMetaData*)cmd;
-(void)clearCachedDataExpiredByMessage:(Msg*)msg;
-(BOOL) isDataLocked:(Msg *)msg;

-(int) minutesUntilSessionExpires;

+(BOOL) needsToRefreshAllViews:(NSDictionary*) pBag;
+(void) sendMsgToAllVisibleViews:(Msg *) msg;

+(void) registerMsgClass:(NSString*) msgIdKey withClass:(Class)fac;

@end
