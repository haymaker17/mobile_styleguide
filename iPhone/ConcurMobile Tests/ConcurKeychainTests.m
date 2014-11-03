//
//  ConcurKeychainTests.m
//  ConcurMobile
//
//  Created by ernest cho on 12/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>

@interface ConcurKeychainTests : XCTestCase

@end

/**
 These tests have no setUp or tearDown.
 */
@implementation ConcurKeychainTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

/**
 Verifies that a time delay breaks a unit test, invalidating our old Keychain unit tests.

 This is because the failure is happening on another thread and XCTest fails to see it.
 */
- (void)testThreadedTimeDelay
{
    double delayInSeconds = 2.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        // This does NOT fail the test cause it's on another thread.
        XCTFail(@"Failure");
    });
}

/**
 Demonstrates how to put the keychain in a bad state.  Note this test fails when using Apple's KeychainItemWrapper v1.2

 The code will fail with an exception in KeychainItemWrapper with error code -25299, errSecDuplicateItem.
 This is because KeychainItemWrapper is saving the data to a field not used to determine uniqueness in keychain.
 http://stackoverflow.com/questions/11614047/what-makes-a-keychain-item-unique-in-ios

 This test passes when a fix is applied to Apple's KeychainItemWrapper v1.2
 Convert the identifier from kSecAttrGeneric to kSecAttrAccount

 http://stackoverflow.com/questions/4891562/ios-keychain-services-only-specific-values-allowed-for-ksecattrgeneric-key/14243924#14243924
 */
- (void)testKeychainItemWrapperIdentifierBug
{
    // save hello world to keychain under testBug1
	KeychainItemWrapper *wrapper1 = [[KeychainItemWrapper alloc] initWithIdentifier:@"testBug1" accessGroup:nil];

    NSString *input1 = @"hello world";
	[wrapper1 setObject:input1 forKey:(__bridge id)kSecValueData];
    NSString *output1 = [wrapper1 objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([input1 isEqualToString:output1], @"Failed to save hello world, %@", output1);

    // attempt to save hello world under testBug2
	KeychainItemWrapper *wrapper2 = [[KeychainItemWrapper alloc] initWithIdentifier:@"testBug2" accessGroup:nil];

    NSString *input2 = @"hello world";
    [wrapper2 setObject:input2 forKey:(__bridge id)kSecValueData];
    NSString *output2 = [wrapper2 objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([input2 isEqualToString:output2], @"Failed to save hello world, %@", output2);

    // clean up after successful test
    [wrapper1 resetKeychainItem];
    [wrapper2 resetKeychainItem];
}

@end
