//
//  Line.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Line : NSObject <NSCoding>

@property (strong, nonatomic) NSString *key;
@property (strong, nonatomic) NSString *name;

+ (Line *)empty;

@end
