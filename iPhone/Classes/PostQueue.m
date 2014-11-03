//
//  PostQueue.m
//  ConcurMobile
//
//  Created by yiwen on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PostQueue.h"
#import "PostMsgInfo.h"
#import "ExSystem.h" 

#import "PostMessageManager.h"

@interface PostQueue (private)
- (void) loadQueue;
+ (Msg*) entityToMsg:(EntityPostMessage*) epMsg;
+ (EntityPostMessage*) msgToEntity:(Msg*)msg;
@end

@implementation PostQueue
@synthesize postMsgs;

static PostQueue * sharedInstance = nil;

#pragma mark Static Methods
+ (PostQueue*) getInstance
{
	if (sharedInstance == nil)
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[PostQueue alloc] initQueue];
                [sharedInstance loadQueue];
			}
		}
	}
	return sharedInstance;
}

+ (BOOL) isImageContent:(NSString*) contentType
{
    return ([@"image/png" isEqualToString:contentType] || [@"image/jpeg" isEqualToString:contentType]);
}

+ (NSString*) getFileRelativePathFromMsgUuid:(NSString*) msgUuid
{
    return [NSString stringWithFormat:@"pq_req_%@", msgUuid];
}

+ (NSString*) getFileFullPath:(NSString*) relativePath
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
    NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:relativePath];
    
    return initFilePath;
}

+ (Msg*) entityToMsg:(EntityPostMessage*) epMsg
{
    __autoreleasing Msg* msg = [[Msg alloc] init];
    msg.uuid = epMsg.uuid;
    msg.idKey = epMsg.idKey;
    msg.method = @"POST";
    msg.header = epMsg.header;
    msg.uri = epMsg.uri;
    if (epMsg.relPostMessageBody != nil)
    {
        msg.bodyData = epMsg.relPostMessageBody.data;
    }
    else if ([epMsg.reqFileInfo length] && [PostQueue isImageContent:msg.contentType])
    {
        epMsg.reqFileInfo = [PostQueue getFileRelativePathFromMsgUuid:msg.uuid]; 
        if ([epMsg.reqFileInfo length])
        {
            NSString* fullPath = [self getFileFullPath:epMsg.reqFileInfo];
            msg.bodyData = [NSData dataWithContentsOfFile:fullPath];
        }
    }
    msg.contentType = epMsg.contentType;
    return msg;
}

+ (EntityPostMessage*) msgToEntity:(Msg*)msg
{
    EntityPostMessage* epMsg = [[PostMessageManager sharedInstance] makeNew];
    epMsg.uuid = msg.uuid;
    epMsg.idKey = msg.idKey;
    epMsg.header = msg.header;
    epMsg.uri = msg.uri;
    if (![PostQueue isImageContent:msg.contentType])
    {
        if ([msg.body length])
        {
            epMsg.relPostMessageBody = [[PostMessageManager sharedInstance] makeNewBody];
            epMsg.relPostMessageBody.data = [msg.body dataUsingEncoding:NSUTF8StringEncoding];
        }
        else if (msg.bodyData != nil)
        {
            epMsg.relPostMessageBody = [[PostMessageManager sharedInstance] makeNewBody];
            epMsg.relPostMessageBody.data = msg.bodyData;
        }
    }
    else if (msg.bodyData != nil)
        epMsg.reqFileInfo = [PostQueue getFileRelativePathFromMsgUuid:msg.uuid];
    epMsg.contentType = msg.contentType;
    return epMsg;
}

#pragma mark Instance Methods
- (id) initQueue
{
    self = [super init];
    if (self)
    {
        self.postMsgs= [[NSMutableArray alloc] initWithCapacity:10];
    }
    return self;
}

- (void) loadQueue
{
    // Clear out post queue for now, since we don't support offline.
    [[PostMessageManager sharedInstance] clearAll];
    return;
    
    // load from core data
    MsgControl *msgCtrl = [ExSystem sharedInstance].msgControl;
    NSArray * allMsgs = [[PostMessageManager sharedInstance] fetchAll:@"EntityPostMessage"];
    for(EntityPostMessage *entity in allMsgs)
    {
        // Recreate msg and insert to 
        Msg* msg = [PostQueue entityToMsg:entity];
        PostMsgInfo* mInfo = [[PostMsgInfo alloc] init:msg messageControl: msgCtrl];
        [postMsgs addObject:mInfo];
        
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"PostQueue::loadMsg(%@ %@ %@)", msg.idKey, msg.uuid, entity.creationDate] Level:MC_LOG_DEBU];

    }
}

- (void) registerPostMsg: (Msg*) msg messageControl:(MsgControl*) msgCtrl
{
    
    PostMsgInfo* mInfo = [[PostMsgInfo alloc] init:msg messageControl: msgCtrl];
    // add msg coredata
    EntityPostMessage* epMsg = [PostQueue msgToEntity:msg];
    epMsg.retried = 0;
    epMsg.creationDate = [NSDate date];
    if (msg.bodyData != nil && [epMsg.reqFileInfo length])
    {
        NSString *initFilePath = [PostQueue getFileFullPath:epMsg.reqFileInfo];
        
        NSError* err = NULL;
        [msg.bodyData writeToFile:initFilePath options:NSDataWritingFileProtectionComplete error:&err];
    }
        
    [[PostMessageManager sharedInstance] saveIt:epMsg];
    if (!([epMsg.idKey isEqualToString:@"RESET_PIN_USER_EMAIL_DATA"] || [epMsg.idKey isEqualToString:@"RESET_PIN_RESET_USER_PIN"]))
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"PostQueue::regMsg(%@ %@ %@)", epMsg.idKey, epMsg.uuid, epMsg.creationDate] Level:MC_LOG_DEBU];
    
    
    [self.postMsgs addObject: mInfo];
    [self restartPostQueue:msgCtrl];  // send msg(s) if connected
    
}

- (void) finishPostMsg: (RequestController*) reqCtrl result:(NSString*) status
{
    if (false && status != nil && [status isEqualToString:@"NETWORK_FAILURE"])
    {
        // MOB-8864 Disable post queue resend.  Need to validate or nil out delegates before retry; app is not ready, suspend this code for now.
        
        // leave the msg in the queue
    }else {
        for (int ix=0; ix<[self count]; ix++) {
            PostMsgInfo* mInfo = (PostMsgInfo*)(self.postMsgs)[ix];
            if (mInfo != nil)
            {
                if ([mInfo owns:reqCtrl])
                {
                    if (!([mInfo.msg.idKey isEqualToString:@"RESET_PIN_USER_EMAIL_DATA"] || [mInfo.msg.idKey isEqualToString:@"RESET_PIN_RESET_USER_PIN"]))
                        [[MCLogging getInstance] log:[NSString stringWithFormat:@"PostQueue::endMsg(%@ %@)", mInfo.msg.idKey, mInfo.msg.uuid] Level:MC_LOG_DEBU];

                    //delete msg coredata
                    [[PostMessageManager sharedInstance] deleteMessage:mInfo.msg.uuid];
                        
                    [self.postMsgs removeObjectAtIndex:ix];
                    break;
                }
            }
        }
    }
}

- (void) restartPostQueue: (MsgControl*) msgCtrl
{
    if (POSTLIVE==1 && [ExSystem connectedToNetwork])
    {
        for (int ix=0; ix<[self count]; ix++) {
            PostMsgInfo* mInfo = (PostMsgInfo*)(self.postMsgs)[ix];
            if (mInfo != nil)
            {
                if ([mInfo msgCtrl]==nil)
                    mInfo.msgCtrl = msgCtrl;
                [mInfo send];
                
                // update retry count
                EntityPostMessage * epMsg = [[PostMessageManager sharedInstance] getMessageByUUID:mInfo.msg.uuid];
                if (epMsg!= nil)
                {
                    epMsg.retried = @([epMsg.retried intValue]+1);
                    [[PostMessageManager sharedInstance] saveIt:epMsg];
                }
            }
        }
    }
    
}

- (int) count
{
    if (self.postMsgs == nil)
        return 0;
    else {
        return [self.postMsgs count];
    }
}

#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
//    [super encodeWithCoder:coder];
    [coder encodeObject:postMsgs forKey:@"PostMsgs"];
}

- (id)initWithCoder:(NSCoder *)coder {
//    self = [super initWithCoder:coder];
    self.postMsgs = [coder decodeObjectForKey:@"PostMsgs"];
    return self;
}

/*- (BOOL) persistQueue
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];

    NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:@"PostQueue.archive"];
    BOOL result = [NSKeyedArchiver archiveRootObject:self toFile:archivePath];
    return result;
}

+ (PostQueue*) restoreQueue
{
    PostQueue *myQueue;
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:@"PostQueue.archive"];
    
    myQueue = [NSKeyedUnarchiver unarchiveObjectWithFile:archivePath];
    return myQueue;
}
*/
@end
