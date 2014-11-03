//
//  GoogleRouteFinderHandler.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "Msg.h"

@protocol GoogleHandlerDelegate <NSObject>

-(void) handleGoogleLocation:(NSMutableDictionary*)dict didFail:(BOOL) didFail;

@end

@interface GoogleRouteFinderHandler : MsgResponderCommon <NSURLConnectionDelegate>
{
	NSXMLParser				*dataParser;
//	NSString				*currentElement, *path;
//    NSMutableString         *buildString;
	NSMutableDictionary     *dict;
	NSString				*isInElement;
    double                  distance;
    BOOL                    didFail, inDuration, inDistance, inStart, inEnd;
    
    NSURLConnection			*conn;
	NSMutableData			*thisData;
    MobileViewController    *mvc;
    id<GoogleHandlerDelegate>	__weak delegate;
}

@property double                  distance;
@property BOOL                    didFail;
@property BOOL              inDuration;
@property BOOL              inDistance;
@property BOOL              inStart;
@property BOOL              inEnd;
@property (strong, nonatomic) NSURLConnection           *conn;
@property (strong, nonatomic) NSMutableData             *thisData;
@property (strong, nonatomic) NSMutableDictionary       *dict;
@property (nonatomic, weak) id<GoogleHandlerDelegate> delegate; 

@property (strong, nonatomic) MobileViewController    *mvc;

//@property (nonatomic, copy) NSString					*currentElement;
//@property (nonatomic, retain) NSString					*path;
//@property (nonatomic, retain) NSMutableString           *buildString;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag;


-(void) makeDirectionRequest:(NSString *)fromLocation toLocation:(NSString *)toLocation;
@end

