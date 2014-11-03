//
//  ExUnitTestDescriber.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/25/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ExUnitTestDescriber : NSObject {
    BOOL isFail;
    NSString    *failString, *testName, *testKey;
    NSDate      *dateRun;
    
}

@property BOOL         isFail;
@property (strong, nonatomic) NSString    *failString;
@property (strong, nonatomic) NSString    *testName;
@property (strong, nonatomic) NSString    *testKey;
@property (strong, nonatomic) NSDate      *dateRun;

-(id) initWithValues:(NSString *) name key:(NSString *)key;
@end
