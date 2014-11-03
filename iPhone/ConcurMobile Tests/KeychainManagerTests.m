//
//  KeychainManagerTests.m
//  ConcurMobile
//
//  Created by ernest cho on 12/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "KeychainManager.h"

@interface KeychainManager (private)

// Do NOT use these directly, they're only public for unit tests.
- (void)saveValue:(NSString *)value forKey:(NSString *)key;
- (NSString *)valueForKey:(NSString *)key;
- (void)clearValueForKey:(NSString *)key;

@end

@interface KeychainManagerTests : XCTestCase
@property (nonatomic, readwrite, strong) KeychainManager *keychainManager;
@end

/**
 These tests verify the KeychainManager is generally working.
 
 These do NOT work on device!  See the following documentation for information on how to set these up for on device unit testing.
 https://developer.apple.com/legacy/library/documentation/DeveloperTools/Conceptual/UnitTesting/02-Setting_Up_Unit_Tests_in_a_Project/setting_up.html#//apple_ref/doc/uid/TP40002143-CH3-SW5

 */
@implementation KeychainManagerTests

- (void)setUp
{
    [super setUp];
    self.keychainManager = [[KeychainManager alloc] init];
    [self.keychainManager clearKeychain];
}

- (void)tearDown
{
    [super tearDown];
    [self.keychainManager clearKeychain];
}

/**
 Verifies that after setup, the keychain is empty.
 */
- (void)testSetup
{
    XCTAssertTrue(self.keychainManager != nil, @"Failed to init keychain manager");
    XCTAssertTrue([self.keychainManager isKeychainEmpty], @"keychain is not empty after setup");
}

/**
 Verifies save and retrieve of a single value works
 */
- (void)testValueSaveAndRetrieve
{
    NSString *key = @"testValueSaveAndRetrieve";
    NSString *value = @"hello world";

    // save value and verify retrieve works
    [self.keychainManager saveValue:value forKey:key];
    NSString *valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue([value isEqualToString:valueInKeychain], @"Value retrieved from keychain does not match the value saved.");
}

/**
 Verifies overwriting a value works
 */
- (void)testValueOverwrite
{
    NSString *key = @"testValueSaveAndRetrieve";
    NSString *value = @"hello world";

    // save value and verify retrieve works
    [self.keychainManager saveValue:value forKey:key];
    NSString *valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue([value isEqualToString:valueInKeychain], @"Value retrieved from keychain does not match the value saved.");

    // overwrite value and verify retreive works
    NSString *value2 = @"goodbye world";
    [self.keychainManager saveValue:value2 forKey:key];
    valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue([value2 isEqualToString:valueInKeychain], @"Value retrieved from keychain does not match the value saved.");
}

/**
 Verifies overwriting a value with nil, clears the value
 */
- (void)testValueOverwriteWithNil
{
    NSString *key = @"testValueOverwriteWithNil";
    NSString *value = @"hello world";

    // save value and verify retrieve works
    [self.keychainManager saveValue:value forKey:key];
    NSString *valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue([value isEqualToString:valueInKeychain], @"Value retrieved from keychain does not match the value saved.");

    // overwrite value and verify retreive works
    NSString *value2 = nil;
    [self.keychainManager saveValue:value2 forKey:key];
    valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue(valueInKeychain == nil, @"Keychain returned a value when nil was expected");
}

/**
 Verifies overwriting a value with the empty string, clears the value
 */
- (void)testValueOverwriteWithEmptyString
{
    NSString *key = @"testValueOverwriteWithEmptyString";
    NSString *value = @"hello world";

    // save value and verify retrieve works
    [self.keychainManager saveValue:value forKey:key];
    NSString *valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue([value isEqualToString:valueInKeychain], @"Value retrieved from keychain does not match the value saved.");

    // overwrite value and verify retreive works
    NSString *value2 = @"";
    [self.keychainManager saveValue:value2 forKey:key];
    valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue(valueInKeychain == nil, @"Keychain returned a value when nil was expected");
}

/**
 Creates a set of valid keychain entries
 */
- (void)useValidTestData
{
    NSDictionary *testData = @{@"A is first": @"foo bar",
                               @"B is second": @"test this",
                               @"C is third": @"quick brown fox jumped over the lazy dog",
                               @"D is fourth": @"1 2 3 4 5 6 7 8 9 10",
                               @"E is fifth": @"hello world"};

    [self createTestData:testData];
}

/**
 Utility method to create keychain test data
 */
- (void)createTestData:(NSDictionary *)testData
{
    KeychainManager *keychainManager = [[KeychainManager alloc] init];
    for (id key in testData) {
        [keychainManager saveValue:[testData objectForKey:key] forKey:key];
    }
}

/**
 Verifies that createTestData works correctly.
 */
- (void)testCreateTestData
{
    NSDictionary *testData = @{@"A is first": @"foo bar",
                               @"B is second": @"test this",
                               @"C is third": @"quick brown fox jumped over the lazy dog",
                               @"D is fourth": @"1 2 3 4 5 6 7 8 9 10",
                               @"E is fifth": @"hello world"};

    [self createTestData:testData];

    // check that the data in keychain matches the data in the dictionary
    for (NSString *key in testData) {
        NSString *valueInKeychain = [self.keychainManager valueForKey:key];
        NSString *valueInDictionary = [testData objectForKey:key];

        XCTAssertTrue([valueInDictionary isEqualToString:valueInKeychain], @"Value stored in keychain does not match the value in the original dictionary");
    }
}

/**
 Verifies that we can overwrite keychain values using sskeychain
 */
- (void)testOverwriteTestData
{
    NSDictionary *testData = @{@"A is first": @"foo bar",
                               @"B is second": @"test this",
                               @"C is third": @"quick brown fox jumped over the lazy dog",
                               @"D is fourth": @"1 2 3 4 5 6 7 8 9 10",
                               @"E is fifth": @"hello world"};

    [self createTestData:testData];

    // check that the data in keychain matches the data in the dictionary
    for (NSString *key in testData) {
        NSString *valueInKeychain = [self.keychainManager valueForKey:key];
        NSString *valueInDictionary = [testData objectForKey:key];

        XCTAssertTrue([valueInDictionary isEqualToString:valueInKeychain], @"Value stored in keychain does not match the value in the original dictionary");
    }

    // Attempt to overwrite the test data
    testData = @{@"OVERWRITE A is first": @"OVERWRITE foo bar",
                 @"OVERWRITE B is second": @"OVERWRITE test this",
                 @"OVERWRITE C is third": @"OVERWRITE quick brown fox jumped over the lazy dog",
                 @"OVERWRITE D is fourth": @"OVERWRITE 1 2 3 4 5 6 7 8 9 10",
                 @"OVERWRITE E is fifth": @"OVERWRITE hello world"};

    [self createTestData:testData];

    // check that the data in keychain matches the data in the dictionary
    for (NSString *key in testData) {
        NSString *valueInKeychain = [self.keychainManager valueForKey:key];
        NSString *valueInDictionary = [testData objectForKey:key];

        XCTAssertTrue([valueInDictionary isEqualToString:valueInKeychain], @"Value stored in keychain does not match the value in the original dictionary");
    }
}

/**
 Verifies that bulk keychain clear and isEmpty works.
 */
- (void)testKeychainClear
{
    [self useValidTestData];
    XCTAssertFalse([self.keychainManager isKeychainEmpty], @"Failed to populate keychain with test data");

    [self.keychainManager clearKeychain];
    XCTAssertTrue([self.keychainManager isKeychainEmpty], @"Failed to clear keychain");
}

/**
 Verifies that bulk keychain clear and isEmpty works.  This version uses SSKeychain.
 */
- (void)testKeychainClearWithSSKeychain
{
    [self useValidTestData];
    XCTAssertFalse([self.keychainManager isKeychainEmpty], @"Failed to populate keychain with test data");

    [self.keychainManager clearKeychainWithSSKeychain];
    XCTAssertTrue([self.keychainManager isKeychainEmpty], @"Failed to clear keychain");
}

/**
 Verifies that requesting an empty item returns as nil
 */
- (void)testRetrieveEmptyItem
{
    NSString *key = @"testRetrieveEmptyItem";

    // attempt to get a value from an empty keychain
    NSString *valueInKeychain = [self.keychainManager valueForKey:key];
    XCTAssertTrue(valueInKeychain == nil, @"Keychain returned a value when nil was expected");
}

/**
 Verifies that keychainManager ignores nil key
 */
- (void)testSaveWithNilKey
{
    NSString *key = nil;
    NSString *value = @"hello world";

    [self.keychainManager saveValue:value forKey:key];
    XCTAssertTrue([self.keychainManager isKeychainEmpty], @"keychain is not empty after saving with nil key");
}

/**
 Verifies that keychainManager ignores empty string key
 */
- (void)testSaveWithEmptyStringKey
{
    NSString *key = @"";
    NSString *value = @"hello world";

    [self.keychainManager saveValue:value forKey:key];
    XCTAssertTrue([self.keychainManager isKeychainEmpty], @"keychain is not empty after saving with empty string key");
}

/**
 Verifies that keychainManager can save with space char key
 */
- (void)testSaveWithSpaceKey
{
    NSString *key = @" ";
    NSString *value = @"hello world";

    [self.keychainManager saveValue:value forKey:key];
    XCTAssertFalse([self.keychainManager isKeychainEmpty], @"keychain is empty after saving with space char key");
}

@end
