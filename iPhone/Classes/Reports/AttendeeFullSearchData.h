//
//  AttendeeFullSearchData.h
//  ConcurMobile
//
//  Created by yiwen on 10/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AttendeeWithFieldsBaseData.h"

@interface AttendeeFullSearchData : AttendeeWithFieldsBaseData //AttendeeBaseData 
{
	NSString				*atnTypeKey;
	NSArray					*searchFields;
    NSString                *expKey;
    NSString                *polKey;
    NSString                *rpeKey;
    NSArray					*attendeeExclusionList; // array of AttendeeData

}

@property (nonatomic, strong) NSString					*atnTypeKey;
@property (nonatomic, strong) NSArray					*searchFields;
@property (nonatomic, strong) NSString					*expKey;
@property (nonatomic, strong) NSString					*polKey;
@property (nonatomic, strong) NSString					*rpeKey;
@property (nonatomic, strong) NSArray					*attendeeExclusionList;

-(NSString *)makeXMLBody;

@end
