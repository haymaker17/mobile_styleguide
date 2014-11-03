//
//  GovTANumbersData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "GovTANumber.h"

@interface GovTANumbersData : MsgResponderCommon
{
    NSMutableArray      *taNumbers;
    GovTANumber         *currentTANum;
}

@property (nonatomic, strong) NSMutableArray        *taNumbers;
@property (nonatomic, strong) GovTANumber           *currentTANum;

@end
