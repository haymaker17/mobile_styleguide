//
//  MobileActionSheet.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface MobileActionSheet : UIActionSheet <UIActionSheetDelegate>
{
	id	mobileActionSheet_originalDelegate;
    NSArray                    *btnIds;  // IDs of other buttons in order of appearance
}

@property (nonatomic, strong) NSArray       *btnIds;

-(NSString*) getButtonId:(NSInteger) buttonIndex;

+(void) dismissAllMobileActionSheets;

@end
