//
//  SystemConfig.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SystemConfig : NSObject
{
	NSMutableDictionary		*carViolationReasons;
	NSMutableDictionary		*hotelViolationReasons;
    NSMutableDictionary		*airViolationReasons;
	NSMutableArray			*officeLocations;
}

@property BOOL isNonRefundableOnly;
@property BOOL checkboxDefault;
@property BOOL showCheckbox;
@property BOOL ruleViolationExplanationRequired;
@property (nonatomic, strong) NSString *nonRefundableMsg;
@property (nonatomic, strong) NSMutableDictionary	*carViolationReasons;
@property (nonatomic, strong) NSMutableDictionary	*hotelViolationReasons;
@property (nonatomic, strong) NSMutableDictionary	*airViolationReasons;
@property (nonatomic, strong) NSMutableArray		*officeLocations;

+(SystemConfig*)getSingleton;
+(void)setSingleton:(SystemConfig*)systemConfig;

@end
