//
//  MsgControl.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/9/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "MsgControl.h"
#import "RequestController.h"
#import "MsgResponder.h"
#import "ArchivedResponder.h"
#import "PostMsgInfo.h"
#import "PostQueue.h"
#import "CacheData.h"
#import "CacheMetaData.h"

// TODO


@interface MsgControl (Private)
// To construct a new instance of a message object
-(Msg*) makeMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly 
ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc;
-(void) routeMsgToResponder:(Msg*) msg;
-(NSString*) getRecordKey:(Msg*) msg;
-(BOOL) isImageMsg:(Msg*)msg;
-(void) saveRecordToDisk:(Msg*)msg;
+(NSString*) filePathFromCacheFileName:(NSString*)filename;
@end

@implementation MsgControl
@synthesize rootVC;

static NSMutableDictionary* msgIdToClassMap = nil;

+(void) registerMsgClass:(NSString*) msgIdKey withClass:(Class)fac
{
    msgIdToClassMap[msgIdKey] = fac;
}

// Initialize msgId to msg class mapping here
+ (void)initialize
{
	if (self == [MsgControl class]) 
	{
        // Perform initialization here.
		if (msgIdToClassMap == nil)
			msgIdToClassMap = [[NSMutableDictionary alloc] init];
    }
}

-(int) minutesUntilSessionExpires
{
	if ([ExSystem sharedInstance].timeLastGoodRequest == nil || [ExSystem sharedInstance].sys.timeOut <=0)
		return 0;
	
	NSDate* now = [NSDate date];
	NSTimeInterval elapsedSeconds = [now timeIntervalSinceDate:[ExSystem sharedInstance].timeLastGoodRequest];
	int elapsedMinutes = elapsedSeconds / 60;
	return ([[ExSystem sharedInstance].sys.timeOut intValue] - elapsedMinutes);
}

//main entry point to get the message rolling
-(MsgResponder*) newMsgResponder:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache
{
	MsgResponder *thing = nil;
	NSObject* msgClass = msgIdToClassMap[msgIdKey];
	if (msgClass != nil)
	{
        thing = [[[msgClass class] alloc] init];
	}
	return thing;
}

-(Msg*) makeMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly 
   ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache Options:(int)msgOptions RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc 
{
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::makeMsg(%@)", msgIdKey] Level:MC_LOG_INFO];
	MsgResponder* thing = [self newMsgResponder:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache];
	__autoreleasing Msg *msg = nil;
	
	{
        msg = [thing newMsg:parameterBag];
		
		[msg setOnlyCached:cacheOnly];
		msg.skipCache = skipCache;
        msg.options = msgOptions;
		msg.responder = thing;
		thing.respondToMvc = mvc;
        
        if (parameterBag != nil && parameterBag[@"MSG_UUID"] != nil)
            msg.uuid = parameterBag[@"MSG_UUID"];
        else
            msg.uuid = [PostMsgInfo getUUID];
	}
	
	return msg;
}

-(Msg*) makeMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly 
   ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc 
{
    return [self makeMsg:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache Options:0 RespondTo:mvc];
}

-(BOOL) isImageMsg:(Msg*)msg
{
    return [msg.idKey isEqualToString:VENDOR_IMAGE] || [msg.idKey isEqualToString:IMAGE] || [msg.idKey isEqualToString:CAR_IMAGE];
}

-(NSString*) getRecordKey:(Msg*) msg
{
    NSString *recordKey = @"0";
    if (msg.parameterBag != nil & (msg.parameterBag)[@"RECORD_KEY"] != nil) 
    {
        recordKey = (msg.parameterBag)[@"RECORD_KEY"];
    }
    return recordKey;
}

//main entry point to get the message rolling
-(NSString*) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache
{
	return [self createMsg:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache RespondTo:nil];
}

-(NSString*) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache Options:(int)msgOptions
{
	return [self createMsg:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache Options:msgOptions RespondTo:nil];
}

-(NSString*) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly 
	 ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc
{
	Msg *msg = [self createMsgOptionalAdd:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache RespondTo:mvc ShouldAdd:YES];
	return msg.uuid;
}

-(NSString*) createMsg:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly 
          ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache Options:(int)msgOptions RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc
{
	Msg *msg = [self createMsgOptionalAdd:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache Options:msgOptions RespondTo:mvc ShouldAdd:YES];
	return msg.uuid;
}

-(Msg*) createMsgOptionalAdd:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly 
		  ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc ShouldAdd:(BOOL)shouldAdd
{
	return [self createMsgOptionalAdd:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache Options:0 RespondTo:mvc ShouldAdd:shouldAdd];
}

-(Msg*) createMsgOptionalAdd:(NSString *)msgIdKey CacheOnly:(NSString *)cacheOnly 
                ParameterBag:(NSMutableDictionary *)parameterBag SkipCache:(BOOL)skipCache Options:(int)msgOptions RespondTo:(NSObject<ExMsgRespondDelegate>*)mvc ShouldAdd:(BOOL)shouldAdd
{
	//main entry point to get the message rolling
	Msg* msg =[self makeMsg:msgIdKey CacheOnly:cacheOnly ParameterBag:parameterBag SkipCache:skipCache Options:msgOptions RespondTo:mvc];
    
    if (shouldAdd)
        [self add:msg];
    return msg;
}

//simple init to set the rvc
-(void) init:(RootViewController *)rootViewController
{
	rootVC = rootViewController;
}

//add the message to the stack, and then go and get the data if you want to
-(void) add:(Msg *)msg
{
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::add(%@)", msg.idKey] Level:MC_LOG_DEBU];

	if(!msg.skipCache)
	{
		CacheMetaData* cmd = [self getCachedData:msg];	//retrieve the cached data from the local store	

		if (![self shouldRequestNewDataForMessage:msg cacheMetaData:cmd])
		{
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::add(%@) Getting cached data.  No need to request new data", msg.idKey] Level:MC_LOG_DEBU];
			return;
		}
        else 
        {
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::add(%@) Getting cached data", msg.idKey] Level:MC_LOG_DEBU];
        }
	}
	
	[self clearCachedDataExpiredByMessage:msg];
	
	if ([msg.onlyCached isEqualToString:@"NO"])  // && [ExSystem connectedToNetwork]
	{
		//lets check and see if we got connectivity back
        //BOOL isOffline = ![ExSystem connectedToNetwork];
        if (![ExSystem connectedToNetwork])
        {
            [self routeMsgToResponder:msg];
            return;
        }
		
		if ([msg.method isEqualToString:@"POST"] &&
			![msg.idKey isEqualToString:AUTHENTICATION_DATA] &&
            ![msg.idKey isEqualToString:CORP_SSO_AUTHENTICATION_DATA])
		{
			[[PostQueue getInstance] registerPostMsg:msg messageControl:self];     // Let the post queue handle msg life cycle
		}
        else //if(!isOffline)
        {
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::add(%@) fetching data from server", msg.idKey] Level:MC_LOG_DEBU];
			RequestController *rc = [RequestController alloc];	//make an instance of the request controller to fetch the data

			if(![self isImageMsg:msg] && ![msg.idKey isEqualToString:@"GET_TRIPIT_CACHE_DATA"])
			{
				Msg *msgReq = [self makeMsg:msg.idKey CacheOnly:@"NO" ParameterBag:msg.parameterBag SkipCache:msg.skipCache Options:msg.options RespondTo:msg.responder.respondToMvc];

                [rc init:msgReq MessageControl:self];

			}
			else
				[rc init:msg MessageControl:self];	



		}//should I hold on to this?  Is it going to be removed from scope?
	}
}


//retrieves the cache data from local store and returns it using normal procedures
 -(CacheMetaData*) getCachedData:(Msg *)msg
{
	CacheMetaData* cmd = nil;
	
	if ([self isImageMsg:msg] && msg.parameterBag != nil)
	{
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::getCachedData(%@) Vendor Image fetch", msg.idKey] Level:MC_LOG_DEBU];
		NSString *pBagName;
		if ([msg.idKey isEqualToString:VENDOR_IMAGE]) 
		{
			pBagName = (msg.parameterBag)[VENDOR_IMAGE];
		}
		else if((msg.parameterBag)[@"IMAGE_CACHE_NAME"] != nil)
		{
			pBagName = (msg.parameterBag)[@"IMAGE_CACHE_NAME"];
		}
		else
		{
			pBagName = (msg.parameterBag)[@"IMAGE_NAME"];
		}
		
		if([self isDataLocked:msg])
			return cmd;
		
		//checks to see if there is cached data for this msg
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:pBagName];
		////NSLog(@"initFilePath=%@", initFilePath);
		NSData *data = [NSData dataWithContentsOfFile:initFilePath]; //on the device, no cached data is found!
		if (data != nil)
		{
			msg.data = data;
			msg.fileInfo = [[NSFileManager defaultManager] attributesOfItemAtPath:initFilePath error:nil];
			(msg.parameterBag)[@"CAME_FROM_CACHE"] = @"Y";
			[self msgDone:msg CameFromCache:@"YES"];
		}
	}
	else
	{
//		[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::getCachedData(%@) Regular Cache fetch", msg.idKey] Level:MC_LOG_DEBU];
		//has this cached file been saved into the meta data?
		NSString *recordKey = [self getRecordKey:msg];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::getCachedData(%@) Record Key = %@", msg.idKey, recordKey] Level:MC_LOG_DEBU];
		
		cmd = [[ExSystem sharedInstance].cacheData getCacheInfo:msg.idKey UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		
		msg.dateOfData = cmd.dateModified;
		
		if(cmd != nil)
		{//a cached file does not exist...
			//checks to see if there is cached data for this msg
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::getCachedData(%@) Cache meta info was found", msg.idKey] Level:MC_LOG_DEBU];
			
			if([self isDataLocked:msg])
				return cmd;
			
            NSString *initFilePath = [MsgControl filePathFromCacheFileName:cmd.fileName];

			__block NSData *data; // = [NSData dataWithContentsOfFile:initFilePath]; //on the device, no cached data is found!
            data = [NSData dataWithContentsOfFile:initFilePath];
            if (data != nil)
            {
                // TODO do we need to make data strong?
                dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
                dispatch_async(queue, ^{ 
                    msg.data = data;
                    msg.fileInfo = [[NSFileManager defaultManager] attributesOfItemAtPath:initFilePath error:nil];
                    (msg.parameterBag)[@"CAME_FROM_CACHE"] = @"Y";
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self msgDone:msg CameFromCache:@"YES"];
                    });
                });
            }
		}
	}
	
	return cmd;
}

-(CacheMetaData *)getCacheMetaData:(Msg *)msg
{
	NSString *recordKey = [self getRecordKey:msg];

	CacheMetaData *cmd = [[ExSystem sharedInstance].cacheData getCacheInfo:msg.idKey UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	return cmd;
}

-(void) parseAndArchiveMsg:(Msg*) msg
{
    [msg.responder respondToXMLData:msg.data withMsg:msg];
    
	if (msg.responseCode == 200)
	{
		if ([msg.responder conformsToProtocol:@protocol(ArchivedResponder)])
		{
			NSObject<ArchivedResponder>* aResp = (NSObject<ArchivedResponder>*)msg.responder;
			[aResp saveToLocalCache:[ExSystem sharedInstance].userName withCacheMeta:[ExSystem sharedInstance].cacheData];
		}
	}
}

+(BOOL) needsToRefreshAllViews:(NSDictionary*) pBag
{
	return [@"YES" isEqualToString:pBag[@"REFRESH_ALL"]];
}

+(NSString*) needsToRefreshAllViewsWithMsg:(NSDictionary*) pBag
{
	return pBag[@"REFRESH_ALL_WITH_MSG_ID"];
}

+(void) sendMsgToAllVisibleViews:(Msg *) msg
{
	NSArray* visibleViews = [ConcurMobileAppDelegate getAllViewControllers];
	for (int ix = 0; visibleViews != nil && ix < [visibleViews count]; ix++)
	{
		UIViewController* vc = visibleViews[ix];
		if ([vc isKindOfClass:MobileViewController.class])
		{
			MobileViewController * mvc = (MobileViewController*) vc;
			[mvc didProcessMessage:msg];
		}
	}
}

-(void) routeMsgToResponder:(Msg*) msg
{
    id<ExMsgRespondDelegate> mvc = msg.responder.respondToMvc;
    
    if(mvc == nil)
        [ConcurMobileAppDelegate refreshTopViewData:msg];
    else// if(mvc != nil)
    {
        // Mvc can be a viewcontroller or manager of resources
        bool isValidMvc = [(NSObject*)mvc isKindOfClass:[MobileViewController class]]?
            [ConcurMobileAppDelegate hasMobileViewController:(MobileViewController*)mvc] 
            : YES;
        if (msg.isCache || isValidMvc)
        {
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) part 3 - Responding To %@::didProcessMessage: (Cache=%@)", msg.idKey, [(NSObject*)mvc class], msg.isCache?@"Y":@"N"] Level:MC_LOG_DEBU];
            
            [mvc didProcessMessage:msg];
        }
        else
        {
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) part 3 - Dropping msg.  View controller not found. (Cache=%@)", msg.idKey, msg.isCache?@"Y":@"N"] Level:MC_LOG_DEBU];
        }
    }
}

+(BOOL) hasCachedData:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey
{
    CacheMetaData* cachedData = [[ExSystem sharedInstance].cacheData getCacheInfo:msgId UserID:userId RecordKey:recordKey];
    
    if (cachedData != nil)
    {
        NSString *filePath = [MsgControl filePathFromCacheFileName:cachedData.fileName];
        BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:filePath];
        return fileExists;
    }
    
    return NO;
}

+(NSString*) filePathFromCacheFileName:(NSString*)filename
{
    if (filename == nil)
        return nil;
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
    NSString *filepath = [documentsDirectory stringByAppendingPathComponent:filename];
    return filepath;
}

-(void) saveRecordToDisk:(Msg*)msg
{
    NSString *recordKey = [self getRecordKey:msg];
    CacheMetaData *cmd = [[ExSystem sharedInstance].cacheData saveCacheMetaData:msg.idKey UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
    NSString *initFilePath = [MsgControl filePathFromCacheFileName:cmd.fileName];
    [msg.data writeToFile:initFilePath atomically:YES]; //dump the data that we just snagged out to the disk
    msg.fileInfo = [[NSFileManager defaultManager] attributesOfItemAtPath:initFilePath error:nil];
    msg.cacheInfo = cmd;
}

-(void)loadArchiveFromCache:(Msg*) msg
{
    NSString* recordKey = [self getRecordKey:msg];

    NSObject<ArchivedResponder> *aResp = (NSObject<ArchivedResponder> *)msg.responder;
    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@_%@_%@) Load from local cache", msg.idKey, recordKey, [ExSystem sharedInstance].userName] Level:MC_LOG_DEBU];
    
    @try 
    {
        [aResp loadFromLocalCache:[ExSystem sharedInstance].userName withCacheMeta:[ExSystem sharedInstance].cacheData];
    }
    @catch (NSException* ne)
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@_%@_%@) Error loading from local cache", msg.idKey, recordKey, [ExSystem sharedInstance].userName] Level:MC_LOG_DEBU];
        
        // Log failed cache id
        [[ExSystem sharedInstance].cacheData clearCorruptedCache:msg.idKey UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        @throw ne; // Rethrow exception to terminate program
    }
}
//OOOOOK!, the message is done, we have data, now tell everyone about it
-(void) msgDone:(Msg *)msg CameFromCache:(NSString *)fromCache
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) From Cache = %@", msg.idKey, fromCache] Level:MC_LOG_DEBU];
	
    NSString *skipParse = (msg.parameterBag)[@"SKIP_PARSE"];
    BOOL bParse = skipParse == nil || [skipParse isEqualToString:@"NO"];
    BOOL isCache = [fromCache boolValue];
    
	if([fromCache isEqualToString:@"YES"])
        msg.isCache = YES;
	else
	{
		msg.isCache = NO;
		// Log last good request time && url is on the concur server
		NSRange range = [msg.uri rangeOfString:[ExSystem sharedInstance].entitySettings.uri];
		if (msg.responseCode == 200 && range.location != NSNotFound)
		{
			// Update the last good request time to now
			[[ExSystem sharedInstance] updateTimeOfLastGoodRequest];
		}
        
        // If SKIP_PARSE, do not set date?
        if (bParse || [self isImageMsg:msg])
            msg.dateOfData = [NSDate date];
	}
    
	if(msg.parameterBag != nil)
    {
        (msg.parameterBag)[@"CAME_FROM_CACHE"] = fromCache;
    }
    
    if (![fromCache isEqualToString:@"YES"] && msg.responseCode == 200 && 
        ![self isImageMsg:msg] && 
        ![msg.idKey isEqualToString:@"CORP_SSO_QUERY_DATA"] && 
        ![msg.idKey isEqualToString:@"CORP_SSO_AUTHENTICATION_DATA"] &&
        ![msg.idKey isEqualToString:@"AUTHENTICATION_DATA"]) // Explicit checks added for authentication msgs to prevent caching. 
    {
        [self saveRecordToDisk:msg];
    }
    
	if (![fromCache isEqualToString:@"YES"] && msg.parameterBag != nil && 
		(msg.responder.respondToMvc != nil || [MsgControl needsToRefreshAllViewsWithMsg:msg.parameterBag] != nil) && ![msg.idKey isEqualToString:CAR_IMAGE]) 
	{
		if (bParse) 
		{ 
		// MOB-10194 core data refactor
        // MOB-11602 Use shouldParseCachedData to handle msgs update core data.
		if(msg.responder != nil && ![msg.responder shouldParseCachedData])
            {
                if(!isCache)
                {
                    [self parseAndArchiveMsg:msg];
                }
            }
            else
            {
                [self parseAndArchiveMsg:msg];
            }
        }
        
		// Need to look for TO_VIEW in pBag
		NSString* newMsgId = [MsgControl needsToRefreshAllViewsWithMsg:msg.parameterBag];
		if ([newMsgId length] && msg.responder != nil)
		{
            if (msg.responseCode == 200) 
            {   // MOB-8222 - no valid data, user already see error alert, no need to propagate msg data.
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) part 1 - Responding To All Visible Views with another msg %@", msg.idKey, newMsgId] Level:MC_LOG_DEBU];
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
                [msg.responder fillInfoToPropagateMsg:pBag forMsgId:newMsgId];
                [self createMsg:newMsgId CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:nil];
            }
			return;
		}
		else
        {
            [self routeMsgToResponder:msg];
        }
    }
	else if ([self isImageMsg:msg])
	{
		if (msg.errBody)
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) Vendor Image CONNECTION FAILED", msg.idKey] Level:MC_LOG_DEBU];
			return;
		}
		else
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) Vendor Image", msg.idKey] Level:MC_LOG_DEBU];
		}
		
		if (msg.parameterBag != nil)
		{
			if (msg.responseCode == 200)
			{
				NSString *pBagName;// = [msg.parameterBag objectForKey:VENDOR_IMAGE];
				if ([msg.idKey isEqualToString:VENDOR_IMAGE]) 
					pBagName = (msg.parameterBag)[VENDOR_IMAGE];
				else if((msg.parameterBag)[@"IMAGE_CACHE_NAME"] != nil)
					pBagName = (msg.parameterBag)[@"IMAGE_CACHE_NAME"];
				else
					pBagName = (msg.parameterBag)[@"IMAGE_NAME"];
                
				NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
				NSString *documentsDirectory = paths[0];
				NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:pBagName];
				
				[msg.data writeToFile:initFilePath atomically:YES]; //dump the data that we just snagged out to the disk
				msg.fileInfo = [[NSFileManager defaultManager] attributesOfItemAtPath:initFilePath error:nil];
			}
		}
        
		if((msg.parameterBag)[@"IMAGE_VIEW"] != nil)
		{
			// MOB-2427 -  UIImage gets deallocated if initialized with msg.data => nil/[msg.data length] == 0 .
			if (msg.responseCode < 400 && msg.data != nil && [msg.data length] > 0)
			{
				UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
				//img = gotImg;
				UIImageView *iv = (msg.parameterBag)[@"IMAGE_VIEW"];
				if(gotImg != nil && iv != nil) {
					iv.image = gotImg;

                    // mark the image view as needing a refresh
                    [iv setNeedsDisplay];
                }
				
			}
		}
		else 
			[self routeMsgToResponder:msg];
	}
	else
	{
        NSString* recordKey = [self getRecordKey:msg];
        if([fromCache isEqualToString:@"YES"] && !msg.skipCache) {
            CacheMetaData *cmd = [[ExSystem sharedInstance].cacheData getCacheInfo:msg.idKey UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
            msg.cacheInfo = cmd;
            msg.dateOfData = cmd.dateModified;
        }
								  
		if (bParse) 
		{ 
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) Parsing XML (Cache=%@)", msg.idKey, msg.isCache?@"Y":@"N"] Level:MC_LOG_DEBU];
			
			if([msg.responder conformsToProtocol:@protocol(ArchivedResponder)] 
			   && [msg.method isEqualToString: @"GET"]
			   && msg.isCache == YES) 
			{
				[self loadArchiveFromCache:msg];
                
				if ([MsgControl needsToRefreshAllViews:msg.parameterBag])
				{
					[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone(%@) Responding To All Visible Views", msg.idKey] Level:MC_LOG_DEBU];
					[MsgControl sendMsgToAllVisibleViews:msg];
				}
				
			}
			else 
			{
                //__block MsgResponder *responder = msg.responder;
                dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
                dispatch_async(queue, ^{ 
					//[responder respondToXMLData:msg.data]; 
                    // MOB-10194 core data refactor
                    // MOB-11602 Use shouldParseCachedData to handle msgs update core data.
                    if(msg.responder != nil && ![msg.responder shouldParseCachedData])
                    {
                        if(!isCache)
                            [self parseAndArchiveMsg:msg];
                    }
                    else 
                        [self parseAndArchiveMsg:msg];
					dispatch_async(dispatch_get_main_queue(), ^{
						[self routeMsgToResponder:msg];
					});
                });
                return;
			}
		}
        
        [self routeMsgToResponder:msg];
	}
}



-(void)saveCache:(NSString *)msgIdKey RecordKey:(NSString *)recordKey Data:(NSData *)webData
{
	if(recordKey == nil)
		recordKey = @"0";
	
	CacheMetaData *cmd = [[ExSystem sharedInstance].cacheData saveCacheMetaData:msgIdKey UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:cmd.fileName];
	
    NSError* err = NULL;
    [webData writeToFile:initFilePath options:NSDataWritingFileProtectionComplete error:&err];
	//todo: need to do something with err...

}


-(BOOL)shouldRequestNewDataForMessage:(Msg*)msg cacheMetaData:(CacheMetaData*)cmd
{
	if (!cmd || !cmd.dateModified)
		return YES;
    
    if (![ExSystem connectedToNetwork]) // If we have cached data (cmd != nil) and we're offline, then don't request more data
        return NO;
	
	if (cmd.needsRefresh)
		return YES;
	
	if (![msg.idKey isEqualToString:@"SUMMARY_DATA"] &&
		![msg.idKey isEqualToString:@"OOPES_DATA"] &&
        ![msg.idKey isEqualToString:ME_LIST_DATA()] &&
		![msg.idKey isEqualToString:@"TRIPS_DATA"] &&
		![msg.idKey isEqualToString:@"ACTIVE_REPORTS_DATA"] &&
        ![msg.idKey isEqualToString:@"TRIP_APPROVAL_LIST_DATA"] &&
		![msg.idKey isEqualToString:@"REPORT_APPROVAL_LIST_DATA"])
	{
		return YES;
	}
	
	// secondsSinceDataWasCached will initially be negative since the current date is being subtracted from a date in the past.
	NSTimeInterval secondsSinceDataWasCached = [cmd.dateModified timeIntervalSinceNow];
	
	// Make secondsSinceDataWasCached positive.
	secondsSinceDataWasCached = 0 - secondsSinceDataWasCached;
	
	const double secondsUntilCachedDataIsTooOld = 2 * 60;	// 2 min * 60 secs per min
	
	return (secondsSinceDataWasCached > secondsUntilCachedDataIsTooOld);
}

-(void)clearCachedDataExpiredByMessage:(Msg*)msg
{
	NSString *recordKey = @"0";
	// For now, only messages that can affect data shown on the home screen
	// (SUMMARY_DATA, OOPES_DATA, and TRIPS_DATA) will be removed from the cache.
	//
    // MOB-12986 :  As a part of phase 1 of the refactor. Add additional server call with ME_LIST_DATA where ever required.
    // Additional server call is only temporary. Phase 2 changes will rip off all references to OOPES_DATA message call.
    //
	if ([msg.idKey isEqualToString:ADD_TO_REPORT_DATA])
	{
		// When an expense is added to a report, the number of expenses in the expense list is reduced.
		// That number is reflected in summary data (SUMMARY_DATA).  The list of expenses is shortened
		// as well (OOPES_DATA).  Since the expense may have been added to a brand new report, the
		// active reports list (ACTIVE_REPORTS_DATA) will also be removed from the cache.
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        // MOB-12986 : Do not need a server call for expense list. When expenses are imported to the report, the entries are updated in coredata. So Viewcontroller gets updated automatically by fetchedresults controller.
		//[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:OOPES_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		//[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ME_LIST_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ACTIVE_REPORTS_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
	else if ([msg.idKey isEqualToString:SAVE_REPORT_DATA]||[msg.idKey isEqualToString:SAVE_REPORT_ENTRY_DATA] 
			 ||[msg.idKey isEqualToString:SAVE_REPORT_RECEIPT2] ||[msg.idKey isEqualToString:SAVE_REPORT_ENTRY_RECEIPT])
	{
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ACTIVE_REPORTS_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];

        if ([msg.idKey isEqualToString:SAVE_REPORT_DATA])
        {   // MOB-4681 update summary after adding report
            ReportData* origRpt = (msg.parameterBag)[@"REPORT"];
            if (origRpt.rptKey == nil)
                [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        }

    }
	else if ([msg.idKey isEqualToString:DELETE_OOP_DATA])   // TODO: MOB-12986 -- Delete this message id
	{
		// When a new expense is deleted, the expense list shrinks (OOPES_DATA) and the expense count
		// in the summary data (SUMAMRY_DATA) changes.
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:OOPES_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
	else if ([msg.idKey isEqualToString:SAVE_OOP_DATA]) // TODO: MOB-12986 -- Delete this message id
	{
		// When a new expense is saved, the expense list grows (OOPES_DATA) and the expense count
		// in the summary data (SUMAMRY_DATA) changes.
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];    
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:OOPES_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
    else if ([msg.idKey isEqualToString:ME_DELETE_DATA])
	{
		// When a new expense is deleted, the expense list shrinks (OOPES_DATA) and the expense count
		// in the summary data (SUMAMRY_DATA) changes.
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        // MOB-12986 : No need to make server call when expense is deleted.
        // View contoller updates the Coredata so expense list is automatically updated
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ME_LIST_DATA() UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
	else if ([msg.idKey isEqualToString:ME_SAVE_DATA])
	{
		// When a new expense is saved, the expense list grows (OOPES_DATA) and the expense count
		// in the summary data (SUMAMRY_DATA) changes.
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        // MOB-12986 : No need to make server call when expense is deleted.
        // View contoller updates the Coredata so expense list is automatically updated    
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ME_LIST_DATA() UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}

	else if ([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA])
	{
		// When an expense image is uploaded, the expense list needs to be refreshed.
		//
		//TODO: If the image is a report entry.report level image, then don't refresh expense list
		
         // MOB-12986 : unsure why refresh expense list is required
        // This might be handled in coredata until then make a server call - remove later
        [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:OOPES_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ME_LIST_DATA() UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
	else if ([msg.idKey isEqualToString:DELETE_REPORT_ENTRY_DATA])
	{
		// When an entry is removed from a report, it is added back to the expense list (OOPES_DATA)
		// causing it to grow and decreasing the expense count in the summary data (SUMMARY_DATA).
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:OOPES_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        // MOB-12986 : This should be taken care in the Report VC. Place a server call for now - > leave OOPES_DATA call for now.
        [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ME_LIST_DATA() UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ACTIVE_REPORTS_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];

	}
	else if ([msg.idKey isEqualToString:SUBMIT_REPORT_DATA] || [msg.idKey isEqualToString:RECALL_REPORT_DATA] || [msg.idKey isEqualToString:DELETE_REPORT_DATA])
	{
		// When a report is submitted, it is removed from the list of active reports (ACTIVE_REPORTS_DATA)
		// causing it to shrink and decreasing the active report count in summary data (SUMMARY_DATA).
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ACTIVE_REPORTS_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        
        if ([msg.idKey isEqualToString:DELETE_REPORT_DATA])
        {
            [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:OOPES_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
            // MOB-12986 : This should be taken care in the Report VC. Place a server call for now.
            // Remove the OOPES_DATA call later
            [[ExSystem sharedInstance].cacheData markAsNeedingRefresh:ME_LIST_DATA() UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
        }
	}
	else if ([msg.idKey isEqualToString:APPROVE_REPORTS_DATA])
	{
		// When a report is approve or rejected (APPROVE_REPORTS_DATA can do either), the list of
		// active reports (also obtained with APPROVE_REPORTS_DATA) grows or shrinks and the
		// active report count in the summary data (SUMMARY_DATA) changes.
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:REPORT_APPROVAL_LIST_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
    else if ([msg.idKey isEqualToString:APPROVE_TRIPS_DATA])
	{
		// When a Trip is approved or rejected (APPROVE_TRIPS_DATA can do either), the list of
		// active trips ( obtained with TRIP_APPROVAL_LIST_DATA) grows or shrinks and the
		// active trips to approve count in the summary data (SUMMARY_DATA) changes.
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:TRIP_APPROVAL_LIST_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
	else if ([msg.idKey isEqualToString:RESERVE_HOTEL] ||
             [msg.idKey isEqualToString:RESERVE_CAR] ||
             [msg.idKey isEqualToString:HOTEL_CANCEL] || // MOB-9206 added more booking/cancel events trigger TRIPS_DATA refresh
             [msg.idKey isEqualToString:AIR_CANCEL] ||
             [msg.idKey isEqualToString:CAR_CANCEL] ||
             [msg.idKey isEqualToString:AMTRAK_CANCEL] ||
             [msg.idKey isEqualToString:AMTRAK_SELL] ||
             [msg.idKey isEqualToString:AIR_SELL]
             )
	{
		// When a hotel is reserved, a new trip might be created for it which will grow
		// the list of trips (TRIPS_DATA) and affect the trip count (SUMMARY_DATA).
		//
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:SUMMARY_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
		[[ExSystem sharedInstance].cacheData markAsNeedingRefresh:TRIPS_DATA UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
	}
}

//X New mobile expense was entered
//X Expense was added to a NEW report (changes list of reports)
//X Expense was added to an existing report (changes amount of existing report--which also shows in report list)
//- Expense was edited, e.g. type changed from Lunch to Dinner
//X Expense was deleted
//X Report was submitted
//- Receipt was added.  (Report will now have an associated receipt)
//X Report was approved (changes list of reports to approve)
//X Report was rejected (changes list of reports to approve)
//X Car was booked.  (changes trips list and trip details)
//X Hotel was booked.  (changes trip list and trip details)
// Rail was booked.  (changes trip list and trip details)
//- Invoices

-(BOOL) isDataLocked:(Msg *)msg
{
    //YES means that protected data is not available due to the file being in background or locked; NO means that we can access the file
    UIApplication *app = [UIApplication sharedApplication];
    if(!app.protectedDataAvailable)
    {
        msg.data = nil;
        msg.fileInfo = nil;
        msg.errBody = @"Protected data is unavailable from the iOS file system";
        msg.errCode = @"123456789";
        [self msgDone:msg CameFromCache:@"YES"];

        return YES;
    }

	return NO;
}
@end
