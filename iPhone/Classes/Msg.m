//
//  Msg.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "Msg.h"
#import "MobileViewController.h"
#import "Config.h"

@implementation Msg

@synthesize idKey;
@synthesize state;
@synthesize uri;
@synthesize xmlString;
//@synthesize pos;
@synthesize data;
@synthesize body;
//@synthesize notifyee;
@synthesize responder;
@synthesize onlyCached;
@synthesize parameterBag;
@synthesize method;
@synthesize contentType;
@synthesize acceptHeader;
@synthesize header;
@synthesize cell;
@synthesize fileInfo, errBody, errCode, responseBody, responseCode, bodyData, skipCache, cacheInfo, isCache, dateOfData;
@synthesize options;
@synthesize uuid;
@synthesize expectedContentLength;
@synthesize retried;
@synthesize numOauthLegs, oauth2AccessToken;
@synthesize request;

#define REQUEST_TIMEOUT_SECONDS 60.0

-(id) init
{
    self = [super init];
    if (self)
    {
        self.idKey = nil;
        self.state = nil;
        self.uri = nil;
        self.onlyCached = @"NO";
        self.data = nil;
        self.responder = nil;
        self.parameterBag = nil;
        self.method = @"GET";
        self.skipCache = NO;
        self.options = 0;
        self.uuid = nil;
        self.expectedContentLength = 0;
        self.retried = NO;
		self.numOauthLegs = 0;
        self.request = nil;

        // Gov airbooking give out time out error.  It appears to take longer than 60 seconds (REQUEST_TIMEOUT_SECONDS)
        // This is a hack to work with Gov air booking.
        // Need to revist with Travel team
        if ([Config isGov])
            self.timeoutInterval = request.timeoutInterval;
        else
            self.timeoutInterval = REQUEST_TIMEOUT_SECONDS;
    }
	return self;
}

-(id)initWithData:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos 
    MessageData:(NSData *)msgData  URI:(NSString *)msgURI  MessageResponder:(MsgResponder *)msgResponder 
    ParameterBag:(NSMutableDictionary *)paramBag
{
    return [self initWithData:msgIdKey State:msgState Position:msgPos MessageData:msgData URI:msgURI MessageResponder:msgResponder ParameterBag:paramBag Options:0];
	
}

-(id)initWithData:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos 
      MessageData:(NSData *)msgData  URI:(NSString *)msgURI  MessageResponder:(MsgResponder *)msgResponder 
ParameterBag:(NSMutableDictionary *)paramBag Options:(int)msgOptions
{
    self = [super init];
	if (self)
    {
        self.idKey = msgIdKey;
        self.state = msgState;
        self.uri = msgURI;
        self.onlyCached = @"NO";
        //self.pos = msgPos;
        self.data = msgData;
        self.responder = msgResponder;
        self.parameterBag = paramBag;
        self.method = @"GET";
        self.skipCache = NO;
        self.options = msgOptions;
        self.retried = NO;
        // Gov airbooking give out time out error.  It appears to take longer than 60 seconds (REQUEST_TIMEOUT_SECONDS)
        // This is a hack to work with Gov air booking.
        // Need to revist with Travel team
        if ([Config isGov])
            self.timeoutInterval = request.timeoutInterval;
        else
            self.timeoutInterval = REQUEST_TIMEOUT_SECONDS;
    }
	return self;
}

-(id)initWithDataAndSkipCache:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos 
                  MessageData:(NSData *)msgData  URI:(NSString *)msgURI  MessageResponder:(MsgResponder *)msgResponder 
                 ParameterBag:(NSMutableDictionary *)paramBag
{
    return [self initWithDataAndSkipCache:msgIdKey State:msgState Position:msgPos MessageData:msgData URI:msgURI MessageResponder:msgResponder ParameterBag:paramBag Options:0];
}


-(id)initWithDataAndSkipCache:(NSString *)msgIdKey State:(NSString *)msgState Position:(NSInteger *)msgPos 
	  MessageData:(NSData *)msgData  URI:(NSString *)msgURI  MessageResponder:(MsgResponder *)msgResponder 
      ParameterBag:(NSMutableDictionary *)paramBag Options:(int)msgOptions
{
    self = [super init];
	if (self)
    {
        self.idKey = msgIdKey;
        self.state = msgState;
        self.uri = msgURI;
        self.onlyCached = @"NO";
        //self.pos = msgPos;
        self.data = msgData;
        self.responder = msgResponder;
        self.parameterBag = paramBag;
        self.method = @"GET";
        self.skipCache = YES;
        self.options = msgOptions;
        self.retried = NO;
        // Gov airbooking give out time out error.  It appears to take longer than 60 seconds (REQUEST_TIMEOUT_SECONDS)
        // This is a hack to work with Gov air booking.
        // Need to revist with Travel team
        if ([Config isGov])
            self.timeoutInterval = request.timeoutInterval;
        else
            self.timeoutInterval = REQUEST_TIMEOUT_SECONDS;
    }
	return self;
}


// This method is only to be used when we have an NSURLHTTPRequest object already available.
-(id)initWithNSURLRequestAndSkipCache:(NSString *)msgIdKey State:(NSString *)msgState 
                  Request:(NSURLRequest *)req  MessageResponder:(MsgResponder *)msgResponder 
                 ParameterBag:(NSMutableDictionary *)paramBag
{
    self = [super init];
   	if (self)
    {
        self.idKey = msgIdKey;
        self.state = msgState;
        self.uri = [req URL].absoluteString;
        self.onlyCached = @"NO";
        self.responder = msgResponder;
        self.parameterBag = paramBag;
        self.method = req.HTTPMethod;
        self.skipCache = YES;
        self.retried = NO;
        self.request = (NSMutableURLRequest *)req; 
        self.timeoutInterval = req.timeoutInterval;
    }
	return self; 
}

-(void)dataLoaded:(NSData *)thisData
{
//	[notifyee respondToFoundData:thisData];
}

- (void)encodeWithCoder:(NSCoder *)coder {
//    [super encodeWithCoder:coder];
    [coder encodeObject:idKey forKey:@"idKey"];
    [coder encodeObject:uri forKey:@"uri"];
    [coder encodeObject:method forKey:@"method"];
    [coder encodeObject:header forKey:@"header"];
    [coder encodeObject:body forKey:@"body"];
    [coder encodeObject:contentType forKey:@"contentType"];
    // We need to make everyone inherit from Archiveable object before
    // we can uncomment the following
//    [coder encodeObject:parameterBag forKey:@"parameterBag"];
    if (responder != nil)
    {
/*        Class respClass = [responder class];
        MsgResponder* dummyResp = [respClass new];
        [coder encodeObject:dummyResp forKey:@"responder"];  
        [dummyResp release];*/
        [coder encodeObject:responder forKey:@"responder"];        
    }
	[coder encodeObject:uuid forKey:@"uuid"];
	[coder encodeBool:retried forKey:@"retried"];
    [coder encodeObject:request forKey:@"request"];
}

// TODO - how to process the response results from offline to online?
- (id)initWithCoder:(NSCoder *)coder {
//    self = [super initWithCoder:coder];
    self.responder = nil;
    
	self.onlyCached = @"NO";
	skipCache = NO;
    
    self.idKey = [coder decodeObjectForKey:@"idKey"];
    self.uri = [coder decodeObjectForKey:@"uri"];
    self.method = [coder decodeObjectForKey:@"method"];
    self.header = [coder decodeObjectForKey:@"header"];
    self.body = [coder decodeObjectForKey:@"body"];
    self.contentType = [coder decodeObjectForKey:@"contentType"];
    self.parameterBag = [coder decodeObjectForKey:@"parameterBag"];
    self.responder = [coder decodeObjectForKey:@"responder"];
	self.uuid = [coder decodeObjectForKey:@"uuid"];
	self.retried = [coder decodeBoolForKey:@"retried"];
    self.request = [coder decodeObjectForKey:@"request"];
    return self;
}

-(BOOL) didConnectionFail
{
    BOOL connectionFailed = (self.responseCode == 0 && !self.isCache && ![self.idKey isEqualToString: @"SHORT_CIRCUIT"]);
    return connectionFailed;
}

@end
