//
//  NSURLRequestAdditions.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 3/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "NSURLRequestAdditions.h"

@implementation NSURLRequest (NSURLRequestAdditions)
-(NSString *)debugDescription
{
    NSString *body = [[NSString alloc] initWithData:self.HTTPBody
                                            encoding:NSUTF8StringEncoding];
    
    NSString *cookies = @"Cookies:\n";
    for (NSHTTPCookie *cookie in [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies])
    {
        cookies = [cookies stringByAppendingString:[NSString stringWithFormat:@"name: '%@'\n",   [cookie name]]];
        cookies = [cookies stringByAppendingString:[NSString stringWithFormat:@"value: '%@'\n",  [cookie value]]];
        cookies = [cookies stringByAppendingString:[NSString stringWithFormat:@"domain: '%@'\n", [cookie domain]]];
        cookies = [cookies stringByAppendingString:[NSString stringWithFormat:@"path: '%@'\n",   [cookie path]]];
    }
    NSLog(@"Cookies in sharedHTTPCookieStorage: %@\n", cookies);
    
    __autoreleasing NSString *desc = [NSString stringWithFormat:@"%@ \nURL: %@\n HTTP Headers: %@\n HTTP Body %@\n, HTTP Method: %@\n",[self class], [self URL], [self allHTTPHeaderFields].description, body, self.HTTPMethod];
    return desc;
}
@end
