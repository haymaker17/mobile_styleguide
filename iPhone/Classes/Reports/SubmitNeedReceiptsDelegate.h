//
//  SubmitNeedReceiptsDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 4/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol SubmitNeedReceiptsDelegate
- (void)cancelSubmitAfterReceipts;
- (void)confirmSubmitAfterReceipts;
@end
