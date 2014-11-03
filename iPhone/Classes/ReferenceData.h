//
//  ReferenceData.h
//  ConcurMobile
//
//  Created by yiwen on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Currency.h"

@interface ReferenceData : NSObject {
	NSMutableArray			*currencies;	// With common ones on top
	NSMutableDictionary		*currencyDict;	// Mapping from code to crn
}
@property (nonatomic, strong) NSMutableArray		*currencies;
@property (nonatomic, strong) NSMutableDictionary	*currencyDict;

+(ReferenceData*)getSingleton;

-(Currency *) getCurrencyAtIndex:(NSUInteger) index;
-(NSString *) getCurrencyCodeAtIndex:(NSUInteger) index;
-(Currency *) getCurrencyByCode:(NSString*) crnCode;

@end
