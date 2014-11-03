//
//  FSClassOfService.h
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface FSClassOfService : NSObject
{
    NSString *cabin;
    int seats;

}

@property (nonatomic, strong) NSString *cabin;
@property int seats;

-(void) setAttributesFrom: (NSDictionary*)dict;

@end
