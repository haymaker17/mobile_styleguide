//
//  CCActivityIndicatorView.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 12/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CCActivityIndicatorView : UIImageView

@property(nonatomic) BOOL                         hidesWhenStopped;           // default is YES. calls -setHidden when animating gets set to NO

@end
