//
//  ExUnitTestDescriber.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/25/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExUnitTestDescriber.h"


@implementation ExUnitTestDescriber
@synthesize isFail, failString, dateRun, testKey, testName;


-(id) initWithValues:(NSString *) name key:(NSString *)key
{
    self.testName = name;
    self.testKey = key;
    self.dateRun = [NSDate date];
    self.isFail = NO;
    self.failString = @"";
    
    return self;
}
@end
