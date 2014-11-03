//
//  GovDocStampVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormViewControllerBase.h"
#import "GovDocAvailableStamps.h"
#import "GovDocumentDetail.h"
#import "GovDocStampVCDelegate.h"

@interface GovDocStampVC : FormViewControllerBase
{
    UILabel                     *lblAwaitingStatus;
    GovDocAvailableStamps       *availStamps;
    
    NSString                    *docType;
    NSString                    *travelerId;
    NSString                    *awaitingStatus;
    NSString                    *docName;
    NSArray                     *reasonCodes;
    
    NSNumber                    *requiresReason;
    NSString                    *selectedStamp;
    
    id<GovDocStampVCDelegate>               __weak _delegate;
}

@property (weak, nonatomic) id<GovDocStampVCDelegate>   delegate;
@property (strong, nonatomic) IBOutlet UILabel			*lblAwaitingStatus;
@property (nonatomic, retain) NSString                  *docType;
@property (nonatomic, retain) NSString                  *travelerId;
@property (nonatomic, retain) NSString                  *awaitingStatus;
@property (nonatomic, retain) NSString                  *docName;
@property (nonatomic, retain) NSNumber                  *requiresReason;
@property (nonatomic, retain) GovDocAvailableStamps     *availStamps;
@property (nonatomic, retain) NSString                  *selectedStamp;
@property (nonatomic, retain) NSArray                   *reasonCodes;

- (void)setSeedData:(GovDocumentDetail*)doc;
- (void)setSeedDelegate:(id<GovDocStampVCDelegate>)del;

@end
