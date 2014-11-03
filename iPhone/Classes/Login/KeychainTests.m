//
//  KeychainTests.m
//  ConcurMobile
//
//  Created by Shifan Wu on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "KeychainTests.h"

@import Security;

@interface KeychainTests ()

@end

@implementation KeychainTests

#pragma mark - Tests


#pragma mark - Tools

- (NSString *)keychainErrorToString: (NSInteger)error
{
    
    NSString *msg = [NSString stringWithFormat:@"%ld",(long)error];
    
    switch (error) {
        case errSecSuccess:
            msg = NSLocalizedString(@"SUCCESS", nil);
            break;
        case errSecDuplicateItem:
            msg = NSLocalizedString(@"ERROR_ITEM_ALREADY_EXISTS", nil);
            break;
        case errSecItemNotFound :
            msg = NSLocalizedString(@"ERROR_ITEM_NOT_FOUND", nil);
            break;
        case -26276: // this error will be replaced by errSecAuthFailed
            msg = NSLocalizedString(@"ERROR_ITEM_AUTHENTICATION_FAILED", nil);
            
        default:
            break;
    }
    
    return msg;
}

@end
