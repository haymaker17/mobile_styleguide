//
//  ExUnitTests.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ExUnitTests : NSObject {
    NSMutableArray              *aTestNames;
    NSMutableDictionary         *dictTests;
    
}

@property (strong, nonatomic) NSMutableArray              *aTestNames;
@property (strong, nonatomic) NSMutableDictionary         *dictTests;


#pragma -
#pragma Unit Test Methods
-(NSMutableArray *) runTest:(NSString *)key;

@end
