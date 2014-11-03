//
//  AttendeeType.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AttendeeType : NSObject
{
	NSString			*atnTypeKey;
	NSString			*atnTypeCode;
    NSString            *atnTypeName;
    
    NSString            *allowEditAtnCount;
    NSString            *isExternal;
}

@property (nonatomic, strong) NSString				*atnTypeKey;
@property (nonatomic, strong) NSString				*atnTypeCode;
@property (nonatomic, strong) NSString				*atnTypeName;
@property (nonatomic, strong) NSString				*allowEditAtnCount;
@property (nonatomic, strong) NSString              *isExternal;

+ (NSMutableDictionary*) getXmlToPropertyMap;

@end
