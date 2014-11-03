//
//  NSString+Validations.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/31/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NSString+Validations.h"

@implementation NSString (Validations)

- (BOOL)isValidEmail{
	//MOB-16302
    //NSString* emailRegex = @"^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$"; // very very restrictive
    NSString *emailRegex = @"[A-Z0-9a-z._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}"; // restrictive
    NSPredicate *emailTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", emailRegex];
    
    return [emailTest evaluateWithObject:self];
}


@end
