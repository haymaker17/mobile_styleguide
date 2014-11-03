//
//  MobileAlertView.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 10/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface MobileAlertView : UIAlertView <UIAlertViewDelegate>
{
	id                  __weak mobileAlertView_originalDelegate;
    NSObject            *eventData;  // Transitional data useful for processing Alert button events
}

@property (weak, readonly) NSString* loggableTitle;
@property (weak, readonly) NSString* loggableMessage;
@property (nonatomic, strong) NSObject *eventData;

+(void) willPresentMobileAlertView:(MobileAlertView*)mav;
+(void) didDismissMobileAlertView:(MobileAlertView*)mav;
+(void) dismissAllMobileAlertViews;

-(void)clearDelegate;

@end
