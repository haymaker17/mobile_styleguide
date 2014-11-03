//
//  KeychainItemWrapperTests.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 10/31/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "KeychainItemWrapper.h"
#import <Security/Security.h>
#import <Security/SecItem.h>

@interface KeychainItemWrapperTests : XCTestCase
@property (nonatomic, readwrite, strong) KeychainItemWrapper *wrapper;
@end

/**
 Some tests to clarify how KeychainItemWrapper works
 */
@implementation KeychainItemWrapperTests

- (void)setUp
{
    [super setUp];
    // we always save the keychain item under unitTest
    self.wrapper = [[KeychainItemWrapper alloc] initWithIdentifier:@"unitTest" accessGroup:nil];
}

- (void)tearDown
{
    // reset the keychain after a test
    [self.wrapper resetKeychainItem];
    [super tearDown];
}

/**
 Verify that Keychain returns an empty value as an empty string.
 */
- (void)testRetrieveUnsavedValue
{
    NSString *output = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    // keychain returns an empty string
    XCTAssertTrue([output isEqualToString:@""], @"Keychain returned a value when empty string was expected, %@", output);
}

/**
 Verify that after saving the space char, Keychain returns the space char
 */
- (void)testSaveAndRetrieveSpaceCharacter
{
    NSString *input = @" ";
    // the key here is more like a data storage type, it's not a map key!
    [self.wrapper setObject:input forKey:(__bridge id)kSecValueData];
    NSString *output = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([input isEqualToString:output], @"Failed to save space character, %@", output);
}

/**
 Verify that after saving "hello world", Keychain returns "hello world"
 */
- (void)testSaveAndRetrieveHelloWorld
{
    NSString *input = @"hello world";
    [self.wrapper setObject:input forKey:(__bridge id)kSecValueData];
    NSString *output = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([input isEqualToString:output], @"Failed to save hello world, %@", output);
}

/**
 Verify that after saving a long string with special chars, Keychain returns the same string
 */
- (void)testSaveAndRetrieveLongString
{
    NSString *input = @"!@#$%^&*() hello world  hello world  hello world hello world";
    [self.wrapper setObject:input forKey:(__bridge id)kSecValueData];
    NSString *output = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([input isEqualToString:output], @"Failed to save long string with special chars, %@", output);
}

/**
 Verify that after saving nil, Keychain returns an empty string indicating that nothing was saved.
 
 Keychain is unable to save nil, but it does not throw an error.
 */
- (void)testSaveAndRetrieveNil
{
    NSString *input = nil;
    [self.wrapper setObject:input forKey:(__bridge id)kSecValueData];
    NSString *output = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([output isEqualToString:@""], @"Keychain returned a value when empty string was expected, %@", output);
}

/**
 Verify that saving nil does not change the value in Keychain.
 
 Keychain is unable to save nil, but it does not throw an error.
 */
- (void)testNilDoesNotOverwriteExistingValue
{
    // save hello world to keychain
    NSString *input1 = @"hello world";
    [self.wrapper setObject:input1 forKey:(__bridge id)kSecValueData];
    NSString *output1 = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([input1 isEqualToString:output1], @"Failed to save hello world, %@", output1);

    // attempt to overwrite hello world with nil
    NSString *input2 = nil;
    [self.wrapper setObject:input2 forKey:(__bridge id)kSecValueData];
    NSString *output2 = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    // overwrite should have failed
    XCTAssertTrue([output2 isEqualToString:input1], @"Saving nil changed the stored value, %@", output2);
}

/**
 Verify that saving the empty string does change the value in Keychain.
 */
- (void)testEmptyStringDoesOverwriteExistingValue
{
    // save hello world to keychain
    NSString *input1 = @"hello world";
    [self.wrapper setObject:input1 forKey:(__bridge id)kSecValueData];
    NSString *output1 = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([input1 isEqualToString:output1], @"Failed to save hello world, %@", output1);

    // overwrite hello world with empty string
    NSString *input2 = @"";
    [self.wrapper setObject:input2 forKey:(__bridge id)kSecValueData];
    NSString *output2 = [self.wrapper objectForKey:(__bridge NSString *)kSecValueData];

    XCTAssertTrue([output2 isEqualToString:input2], @"Saving empty string failed to overwrite the stored value, %@", output2);
}

@end
