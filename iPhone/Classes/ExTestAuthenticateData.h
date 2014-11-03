//
//  ExTestAuthenticateData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExTestBase.h"

@interface ExTestAuthenticateData : ExTestBase {
    NSMutableArray      *aFails;
    
}

-(BOOL) runLoginResultUnitTest;
-(BOOL) runPositiveUnitTest;
-(NSString *) fetchLoginResult;
-(NSString *) dumpFails;
@end
