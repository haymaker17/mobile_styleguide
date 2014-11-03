//
//  CardButton.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface CardButton : UIButton {
	NSString *cardKey;
}

@property (nonatomic,strong) NSString *cardKey;
@end
