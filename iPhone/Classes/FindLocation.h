//
//  FindLocation.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "LocationResult.h"

@interface FindLocation : MsgResponder
{
	NSString				*currentElement;
	NSString				*path;
	NSString				*address;
	LocationResult			*currentLocationResult;
	NSMutableArray			*locationResults;
    NSMutableString         *buildString;
}

@property (nonatomic, strong) NSMutableString         *buildString;
@property (nonatomic, strong) NSString *currentElement;
@property (nonatomic, strong) NSString *path;
@property (nonatomic, strong) NSString *address;
@property (nonatomic, strong) LocationResult *currentLocationResult;
@property (nonatomic, strong) NSMutableArray* locationResults;

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag;


@end
