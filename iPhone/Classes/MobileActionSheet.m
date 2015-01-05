//
//  MobileActionSheet.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "MobileActionSheet.h"
#import "MCLogging.h"


@implementation MobileActionSheet

@synthesize btnIds;

static NSMutableArray* sheetsCurrentlyShowing;



-(NSString*) getButtonId:(NSInteger) buttonIndex
{
    if (btnIds == nil)
        return nil;
    
    NSInteger offset = buttonIndex;
    
    if (self.firstOtherButtonIndex > 0)
        offset = buttonIndex - self.firstOtherButtonIndex;

    if (offset >=0 && offset <[btnIds count])
        return btnIds[offset];
    
    return nil;
}

#pragma mark -
#pragma mark Static Methods
+(void) willPresentMobileActionSheet:(MobileActionSheet*)mas
{
	@synchronized (self)
	{
		if (sheetsCurrentlyShowing == nil) 
		{
			sheetsCurrentlyShowing = [[NSMutableArray alloc] init];
		}
		
		[sheetsCurrentlyShowing addObject:mas];
	}
}

+(void) didDismissMobileActionSheet:(MobileActionSheet*)mas
{
	@synchronized (self)
	{
		if (sheetsCurrentlyShowing == nil) 
		{
			return;
		}
		
		[sheetsCurrentlyShowing removeObject:mas];
	}
}

+(void) dismissAllMobileActionSheets
{
	[[MCLogging getInstance] log:@"MobileActionSheet::dismissAllMobileActionSheets" Level:MC_LOG_DEBU];
	
	@synchronized (self)
	{
		if (sheetsCurrentlyShowing != nil)
		{
			while ([sheetsCurrentlyShowing count] > 0)
			{
				MobileActionSheet* mas = sheetsCurrentlyShowing[0];
				mas.delegate = nil;
                // MOB-10615 Use cancelButtonIndex to dismiss actionsheet.
                NSInteger cancelButtonIndex = 0;
                if (mas.cancelButtonIndex > 0)
                    cancelButtonIndex = mas.cancelButtonIndex;
				[mas dismissWithClickedButtonIndex:cancelButtonIndex animated:NO];
				[sheetsCurrentlyShowing removeObject:mas];
			}
		}
	}
}


#pragma mark -
#pragma mark Tracking Methods
-(void) onShow
{
	// Turns out that show methods can be called multiple times.  For example, UIActionSheet::showFromRect:inView calls UIActionSheet::showInView.
	// We only want to set mobileActionSheet_originalDelegate to super.delegate the first time.  (Otherwise, the second call will set it to self.)
	if (super.delegate == self)
	{
		return;
	}

	[[MCLogging getInstance] log:@"MobileActionSheet: showing action sheet with the following buttons:" Level:MC_LOG_DEBU];
	for (int i = 0; i < self.numberOfButtons; i++)
	{
		NSString *buttonTitle = [self buttonTitleAtIndex:i];
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"    button at index %i with title: %@", i, (buttonTitle != nil ? buttonTitle : @"<no title>")] Level:MC_LOG_DEBU];
	}
	
	mobileActionSheet_originalDelegate = super.delegate;
	super.delegate = self;
}


#pragma mark -
#pragma mark UIActionSheet Overrides
- (void)showFromBarButtonItem:(UIBarButtonItem *)item animated:(BOOL)animated
{
	[self onShow];
	[super showFromBarButtonItem:item animated:animated];
}

- (void)showFromRect:(CGRect)rect inView:(UIView *)view animated:(BOOL)animated
{
	[self onShow];
	[super showFromRect:rect inView:view animated:animated];
}

- (void)showFromTabBar:(UITabBar *)view
{
	[self onShow];
	[super showFromTabBar:view];
}

- (void)showFromToolbar:(UIToolbar *)view
{
	[self onShow];
	[super showFromToolbar:view];
}

- (void)showInView:(UIView *)view
{
	[self onShow];
	[super showInView:view];
}


#pragma mark -
#pragma mark UIActionSheetDelegate Methods
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if (mobileActionSheet_originalDelegate != nil && [mobileActionSheet_originalDelegate respondsToSelector:@selector(actionSheet:clickedButtonAtIndex:)])
	{
		[mobileActionSheet_originalDelegate actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
	}
}

- (void)willPresentActionSheet:(UIActionSheet *)actionSheet
{
	[MobileActionSheet willPresentMobileActionSheet:self];

	if (mobileActionSheet_originalDelegate != nil && [mobileActionSheet_originalDelegate respondsToSelector:@selector(willPresentActionSheet:)])
	{
		[mobileActionSheet_originalDelegate willPresentActionSheet:actionSheet];
	}
}

- (void)didPresentActionSheet:(UIActionSheet *)actionSheet
{
	if (mobileActionSheet_originalDelegate != nil && [mobileActionSheet_originalDelegate respondsToSelector:@selector(didPresentActionSheet:)])
	{
		[mobileActionSheet_originalDelegate didPresentActionSheet:actionSheet];
	}
}

- (void)actionSheet:(UIActionSheet *)actionSheet willDismissWithButtonIndex:(NSInteger)buttonIndex
{
	if (mobileActionSheet_originalDelegate != nil && [mobileActionSheet_originalDelegate respondsToSelector:@selector(actionSheet:willDismissWithButtonIndex:)])
	{
		[mobileActionSheet_originalDelegate actionSheet:actionSheet willDismissWithButtonIndex:buttonIndex];
	}
}

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
	if (buttonIndex < 0)
	{
		[[MCLogging getInstance] log:@"MobileActionSheet::actionSheet:didDismissWithButtonIndex.  Action sheet cancelled." Level:MC_LOG_DEBU];
	}
	else
	{
		NSString* buttonTitle = [self buttonTitleAtIndex:buttonIndex];
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"MobileActionSheet::actionSheet:didDismissWithButtonIndex: buttonIndex = %li, title: %@", (long)buttonIndex, (buttonTitle != nil ? buttonTitle : @"<no title>")] Level:MC_LOG_DEBU];
	}

	if (mobileActionSheet_originalDelegate != nil && [mobileActionSheet_originalDelegate respondsToSelector:@selector(actionSheet:didDismissWithButtonIndex:)])
	{
		[mobileActionSheet_originalDelegate actionSheet:actionSheet didDismissWithButtonIndex:buttonIndex];
	}
	
	[MobileActionSheet didDismissMobileActionSheet:self];
}

- (void)actionSheetCancel:(UIActionSheet *)actionSheet
{
	if (mobileActionSheet_originalDelegate != nil && [mobileActionSheet_originalDelegate respondsToSelector:@selector(actionSheetCancel:)])
	{
		[mobileActionSheet_originalDelegate actionSheetCancel:actionSheet];
	}
}


@end
