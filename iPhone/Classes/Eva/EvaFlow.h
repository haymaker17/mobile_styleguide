//
//  EvaFlow.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface EvaFlow : NSObject

@property  EvaSearchCategory evaFlowType;
@property (nonatomic,strong) NSString *sayIt;
@property (nonatomic,strong) NSArray *evaRelatedLocations;
@property  EvaSearchCategory actionType;

-(id)initWithDict:(NSDictionary *)dictionary;

@end
