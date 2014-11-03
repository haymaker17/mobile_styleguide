//
//  HotelsNearMeListSectionInfo.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractDataSourceDelegate.h"  

@interface ConcreteDataSourceSectionInfo : NSObject <AbstractDataSourceSectionInfo>

-(instancetype)initWithArray:(NSArray *)items;

@end
