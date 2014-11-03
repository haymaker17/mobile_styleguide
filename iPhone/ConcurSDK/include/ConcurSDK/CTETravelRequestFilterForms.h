//
//  CTETravelRequestFilterForms.h
//  ConcurSDK
//
//  Created by Laurent Mery on 15/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class CTEField;
@class CTETravelRequest;

@interface CTETravelRequestFilterForms : NSObject

/**
 return formated value
  */
+ (NSString*)getFormatedValueFromField:(CTEField*)field withDatas:(CTETravelRequest*)datas andContext:(NSDictionary*)context;

+ (NSArray*)fields:(NSArray*)fields filteredByContext:(NSDictionary*)context;

@end
