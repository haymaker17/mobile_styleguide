//
//  Msg.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgData.h"
#import "MsgResponder.h"
#import "CacheMetaData.h"
#import <Foundation/NSDate.h>

typedef enum messageOptions { 
    SILENT_ERROR = 1<<0,
    NO_RETRY = 1<<1
} MessageOptions;

@interface Msg : NSObject 
{
	NSString				*idKey, *state, *uri, *xmlString, *onlyCached, *method, *header, *contentType, *acceptHeader, *body, *errCode, *errBody, *responseBody;
	NSInteger				*pos;
	NSData					*data;
	MsgResponder			*responder;
	NSMutableDictionary		*parameterBag;
	NSDictionary			*fileInfo;
	UITableViewCell			*cell;
	NSData                  *bodyData;
	int						responseCode;
	BOOL					skipCache, isCache;
    int                     options;
	NSDate					*dateOfData;
	CacheMetaData			*cacheInfo;
	NSString				*uuid;	// Unique identifier for each instance of message
    int                     expectedContentLength;  // Value of X-ExpectedContentLength header
	BOOL					retried;
	int						numOauthLegs; // Zero (default) for no oauth.  Two or three for two (client authentication) or three legged (client and user authentication) oauth, respectively.
    NSString                *oauth2AccessToken;
    NSMutableURLRequest            *request;
}

@property (nonatomic, strong) NSString *idKey; //this is the name of the view, not the view controller
@property (nonatomic, strong) NSString *state;
@property (nonatomic, strong) NSString *method;
@property (nonatomic, strong) NSString *header;
@property (nonatomic, strong) NSString *contentType;
@property (nonatomic, strong) NSString *acceptHeader;
@property (nonatomic, strong) NSString *body;
@property (nonatomic, strong) NSString *uri;
@property (nonatomic, strong) NSString *xmlString;
@property (nonatomic, strong) NSString *onlyCached;
@property (nonatomic, strong) MsgResponder *responder;
@property (nonatomic, strong) NSMutableDictionary *parameterBag;
@property (nonatomic, strong) UITableViewCell			*cell;
@property (nonatomic, strong) NSDictionary *fileInfo;
@property (nonatomic, strong) NSString *errCode;
@property (nonatomic, strong) NSString *errBody;
@property int responseCode;
@property BOOL skipCache;
@property int options;
@property (nonatomic, strong) NSString *responseBody;
@property (nonatomic, strong) NSData *bodyData;
@property (nonatomic, strong) CacheMetaData			*cacheInfo;
@property (nonatomic, strong) NSDate				*dateOfData;
@property BOOL isCache;
@property BOOL retried;
@property int numOauthLegs;
@property (nonatomic, strong) NSString *oauth2AccessToken;

//@property (nonatomic, retain) NSInteger *pos;
@property (nonatomic, strong) NSData *data;
//@property (nonatomic, retain) MobileViewController *notifyee;
@property (strong, nonatomic) NSString		*uuid;
@property int                       expectedContentLength;
@property (nonatomic, strong) NSMutableURLRequest *request;
@property (nonatomic) NSTimeInterval timeoutInterval;

-(id) init;

-(id)initWithData:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos MessageData:(NSData *)msgData URI:(NSString *)msgURI MessageResponder:(MsgResponder *)msgResponder ParameterBag:(NSMutableDictionary *)paramBag;

-(id)initWithData:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos MessageData:(NSData *)msgData URI:(NSString *)msgURI MessageResponder:(MsgResponder *)msgResponder ParameterBag:(NSMutableDictionary *)paramBag Options:(int)msgOptions;


-(id)initWithDataAndSkipCache:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos MessageData:(NSData *)msgData  URI:(NSString *)msgURI  MessageResponder:(MsgResponder *)msgResponder ParameterBag:(NSMutableDictionary *)paramBag;

-(id)initWithDataAndSkipCache:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos MessageData:(NSData *)msgData  URI:(NSString *)msgURI  MessageResponder:(MsgResponder *)msgResponder ParameterBag:(NSMutableDictionary *)paramBag Options:(int)msgOptions;


-(id)initWithNSURLRequestAndSkipCache:(NSString *)msgIdKey State:(NSString *)msgState 
                              Request:(NSURLRequest *)req  MessageResponder:(MsgResponder *)msgResponder 
                         ParameterBag:(NSMutableDictionary *)paramBag;

-(void)dataLoaded:(NSData *)thisData;

- (void)encodeWithCoder:(NSCoder *)coder;
- (id)initWithCoder:(NSCoder *)coder;

-(BOOL) didConnectionFail;

@end
