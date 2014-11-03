//
//  GovDocPerdiemTDY.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@interface GovDocPerdiemTDY : NSObject
{
    NSString                *rate;
    NSString                *perdiemLocation;
    NSDate                  *beginTdy;
    NSDate                  *endTdy;
}

@property (nonatomic, strong) NSString              *perdiemLocation;
@property (nonatomic, strong) NSString              *rate;
@property (nonatomic, strong) NSDate                *beginTdy;
@property (nonatomic, strong) NSDate                *endTdy;

@end
