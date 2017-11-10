## 2.1.1 (YYYY-MM-DD)

### Bug fixes

* `getItemId(int)` returned the item index instead of `NO_ID` as the default implementation (#132).

## 2.1.0 (2017-05-16)

### Bug fixes

* Now `RealmBaseAdapter` behaves as empty if the `adapterData` is not valid (#112).

### Enhancements

* Added `updateOnModification` to `RealmRecyclerViewAdapter` constructor (#107).

## 2.0.0 (2017-02-28)

Works with Realm Java 3.x.

### Breaking changes

* Removed `RealmBaseAdapter(@Nonnull Context context, @Nullable OrderedRealmCollection<T> data)`.
* Removed `RealmRecyclerViewAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<T> data, boolean autoUpdate)`.
* Removed `RealmBaseAdapter.inflater`.
* Removed `RealmBaseAdapter.context`.
* Removed `RealmRecyclerViewAdapter.inflater`.
* Removed `RealmRecyclerViewAdapter.contex`.

### Enhancements

* Added fine grained notification support to `RealmRecyclerViewAdapter`.

## 1.5.0

### Deprecated

* `RealmBaseAdapter(@Nonnull Context context, @Nullable OrderedRealmCollection<T> data)`
use `RealmBaseAdapter(@Nullable OrderedRealmCollection<T> data)` instead

* `RealmRecyclerViewAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<T> data, boolean autoUpdate)`
use `RealmRecyclerViewAdapter(@Nullable OrderedRealmCollection<T> data, boolean autoUpdate)` instead.

### Credits

* Thanks to Bhargav Mogra (@bhargavms) for identifying the unnecessary dependency on context and cleaning up.


## 1.4.1

### Bug fixes

* Fixed crash of `RealmRecyclerViewAdapter#updateData()` when the `adapterData` in it was already invalid (#58).


## 1.4.0

### Enhancements

* Removed transitive dependency to realm-android-library in order to support realm-android-library-object-server.


## 1.3.0

### Enhancements

* Added Android Support Annotations for all public adapter methods.

### Credits

* Lars Grefer (@larsgrefer) for adding the support annotations.


## 1.2.2

### Bug fixes

* `RealmRecyclerViewAdapter` uses RealmModel instead of RealmObject (#29).


## 1.2.1

### Bug fixes

* Fixed crash when auto-updating RealmResults (#25).


## 1.2.0

### Enhancements

* Removed `appcompat-v7` from the library dependencies list.


## 1.1.0

### Enhancements

* `RealmRecyclerViewAdapter`, a new adapter base class for `RecyclerView`s.

### Bug fixes

* Removed `allowBackup` and `supportsRtl` from the Android manifest file.

### Credits

* Paweł Surówka (@thesurix) for adding the `RealmRecyclerViewAdapter`.
* Mitchell Tilbrook (@marukami) for cleaning up `AndroidManifest.xml`.`


## 1.0.1

### Bug fixes

* Javadoc and sources are now properly distributed as well.
* RealmBaseAdapter: Fixed listeners potentially leaking when using a RealmResults.


## 1.0.0

Initial release.
