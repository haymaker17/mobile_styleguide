//
//  ExUnitTests.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExUnitTests.h"
#import "ExTestAuthenticateData.h"

@implementation ExUnitTests
@synthesize aTestNames, dictTests;

static NSMutableDictionary* msgIdToClassMap = nil;


-(id) init
{
    self = [super init];
    if (self)
    {
        if (msgIdToClassMap == nil)
            msgIdToClassMap = [[NSMutableDictionary alloc] init];
    
        [msgIdToClassMap removeAllObjects];
        msgIdToClassMap[@"Authenticate Data"] = [ExTestAuthenticateData class];

        self.dictTests = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"testSettingsData:",@"Settings Data", @"testAuthenticateData:", @"Authenticate Data", nil];

        self.aTestNames = [NSMutableArray arrayWithArray: [dictTests keysSortedByValueUsingSelector:@selector(compare:)]];
    }
    return self;
}


#pragma -
#pragma Unit Test Methods
-(NSMutableArray *) runTest:(NSString *)key
{
    NSMutableArray *a = nil;
	ExTestBase *thing = nil;
	NSObject* msgClass = msgIdToClassMap[key];
	if (msgClass != nil)
	{
        thing = [[[msgClass class] alloc] init];
        a = [[NSMutableArray alloc] initWithArray:[thing runTests]];
	}
    

	return a;
}



@end
