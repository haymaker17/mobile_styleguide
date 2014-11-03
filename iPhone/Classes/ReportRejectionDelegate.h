//
//  ReportRejectionDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@protocol ReportRejectionDelegate

- (void)rejectedWithComment:(NSString*)comment;
- (void)rejectionCancelled;

@end
