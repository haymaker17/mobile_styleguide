//
//  AttendeeSearchData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AttendeeBaseData.h"
#import "AttendeeData.h"

@interface AttendeeSearchData : AttendeeBaseData
{
	NSString				*query;
	NSArray					*attendeeExclusionList; // array of AttendeeData
}

@property (nonatomic, strong) NSString					*query;
@property (nonatomic, strong) NSArray					*attendeeExclusionList;

+(NSString *)makeExclusionXml:(NSArray*) atnExclusionList;
-(NSString *)makeXMLBody;

@end
