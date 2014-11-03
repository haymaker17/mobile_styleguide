//
//  GovListFieldEditVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ListFieldEditVC.h"

@interface GovListFieldEditVC : ListFieldEditVC
{
    NSMutableDictionary     *formAttributes; // non-fields related attributes inside Get response
}

@property(nonatomic, strong) NSMutableDictionary            *formAttributes;
@end
