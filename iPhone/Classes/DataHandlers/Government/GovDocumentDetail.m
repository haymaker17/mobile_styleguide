//
//  GovDocumentDetail.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocumentDetail.h"

@implementation GovDocumentDetail

-(id)init
{
    if (self = [super init])
    {
        self.accountCodes = [[NSMutableArray alloc] init];
        self.reasonCodes = [[NSMutableArray alloc] init];
        self.expenses = [[NSMutableArray alloc] init];
        self.exceptions = [[NSMutableArray alloc] init];
        self.perdiemTDY = [[NSMutableArray alloc] init];
        self.tripTypeCodes = [[NSMutableArray alloc] init];
    }
    return self;
}
@end
