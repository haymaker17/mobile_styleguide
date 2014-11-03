//
//  MsgData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/7/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "XMLBase.h"


@interface MsgData : NSObject 
{
	NSString			*type;	//What type of message this is
	UIViewController	*vc;	//The view controller that this base will update
	XMLBase				*base;	//The XML base data class that will do the parsing of the response
	NSString			*url;	//The URL in string form of the web service call to make
	NSString			*xml;	//The data converted into an XML string
}

@property (strong, nonatomic) XMLBase *base;

-(NSString *)getXML;

@end
