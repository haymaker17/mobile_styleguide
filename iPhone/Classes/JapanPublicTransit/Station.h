//
//  Station.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

@interface Station : NSObject <NSCoding>

@property (strong, nonatomic) NSString *key;
@property (strong, nonatomic) NSString *name;

+ (Station *)empty;
+ (Station *)none;

@end
