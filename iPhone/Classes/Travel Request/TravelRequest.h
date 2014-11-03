//
//  TravelRequest.h
//  ConcurMobile
//
//  Created by laurent mery on 18/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TravelRequest : NSObject

+(NSDictionary*)contextTravelRequest:(NSDictionary*)tvrInfos;

+(NSString*)formatedDateMdyyyy:(NSString*)value withTemplate:(NSString*)template;
+(NSString*)formatedDateYYYYMMddTHHmmss:(NSString*)value withTemplate:(NSString*)template;

+(NSString*)formatAmount:(NSString*)value withCurrency:(NSString*)crnCode;
@end
